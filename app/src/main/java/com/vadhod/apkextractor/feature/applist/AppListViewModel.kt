package com.vadhod.apkextractor.feature.applist

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vadhod.apkextractor.AppContainer
import com.vadhod.apkextractor.core.model.AppEntry
import com.vadhod.apkextractor.core.model.ExportFormat
import com.vadhod.apkextractor.core.model.SortOrder
import com.vadhod.apkextractor.core.model.sortedByOrder
import com.vadhod.apkextractor.core.util.AppResult
import com.vadhod.apkextractor.data.extract.SafApkSink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Which tab is showing. */
enum class AppTab { USER, SYSTEM }

data class ExtractItemResult(val label: String, val ok: Boolean, val message: String?)

data class ExtractionUiState(
    val total: Int,
    val completed: Int,
    val currentLabel: String,
    val fraction: Float,
    val results: List<ExtractItemResult> = emptyList(),
    val done: Boolean = false,
) {
    val failures: Int get() = results.count { !it.ok }
}

data class AppListUiState(
    val loading: Boolean = true,
    val query: String = "",
    val sortOrder: SortOrder = SortOrder.NAME_ASC,
    val tab: AppTab = AppTab.USER,
    val userApps: List<AppEntry> = emptyList(),
    val systemApps: List<AppEntry> = emptyList(),
    val selected: Set<String> = emptySet(),
    val extraction: ExtractionUiState? = null,
) {
    val selectionMode: Boolean get() = selected.isNotEmpty()

    /** Apps for the active tab, filtered by query and sorted. */
    val visibleApps: List<AppEntry>
        get() {
            val base = if (tab == AppTab.USER) userApps else systemApps
            val filtered = if (query.isBlank()) base else base.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
            return filtered.sortedByOrder(sortOrder)
        }
}

class AppListViewModel(
    private val appContext: Context,
    private val container: AppContainer,
) : ViewModel() {

    private val _state = MutableStateFlow(AppListUiState())
    val state: StateFlow<AppListUiState> = _state.asStateFlow()

    @Volatile private var bundleSplitsByDefault = true

    init {
        viewModelScope.launch {
            container.settingsRepository.settings.collect { s ->
                bundleSplitsByDefault = s.bundleSplitsByDefault
                _state.update { it.copy(sortOrder = s.sortOrder) }
            }
        }
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            val partitioned = container.packageRepository.getPartitioned()
            _state.update {
                it.copy(
                    loading = false,
                    userApps = partitioned.user,
                    systemApps = partitioned.system,
                )
            }
        }
    }

    fun onQueryChange(q: String) = _state.update { it.copy(query = q) }

    fun onTabSelected(tab: AppTab) = _state.update { it.copy(tab = tab) }

    fun onSortSelected(order: SortOrder) {
        viewModelScope.launch { container.settingsRepository.setSortOrder(order) }
    }

    fun toggleSelection(pkg: String) = _state.update {
        val next = if (pkg in it.selected) it.selected - pkg else it.selected + pkg
        it.copy(selected = next)
    }

    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    fun selectAllVisible() = _state.update {
        it.copy(selected = it.selected + it.visibleApps.map { app -> app.packageName })
    }

    fun appsForSelection(): List<AppEntry> {
        val s = _state.value
        val all = s.userApps + s.systemApps
        return all.filter { it.packageName in s.selected }
    }

    private fun defaultFormat(app: AppEntry): ExportFormat =
        if (app.isSplit && bundleSplitsByDefault) ExportFormat.BundleApks else ExportFormat.SingleBaseApk

    /** Extracts [apps] into the SAF [treeUri]. [forceFormat] overrides the per-app default. */
    fun extract(apps: List<AppEntry>, treeUri: Uri, forceFormat: ExportFormat? = null) {
        if (apps.isEmpty()) return
        viewModelScope.launch {
            val sink = runCatching { SafApkSink(appContext, treeUri) }.getOrElse { e ->
                _state.update {
                    it.copy(
                        extraction = ExtractionUiState(
                            total = apps.size, completed = 0, currentLabel = "",
                            fraction = 0f, done = true,
                            results = apps.map { a -> ExtractItemResult(a.label, false, "Folder error: ${e.message}") },
                        ),
                    )
                }
                return@launch
            }

            val results = mutableListOf<ExtractItemResult>()
            apps.forEachIndexed { index, app ->
                _state.update {
                    it.copy(
                        extraction = ExtractionUiState(
                            total = apps.size,
                            completed = index,
                            currentLabel = app.label,
                            fraction = 0f,
                            results = results.toList(),
                        ),
                    )
                }
                val format = forceFormat ?: defaultFormat(app)
                val result = container.apkExtractor.extract(app, format, sink) { frac ->
                    _state.update { st ->
                        st.extraction?.let { st.copy(extraction = it.copy(fraction = frac)) } ?: st
                    }
                }
                results += when (result) {
                    is AppResult.Success -> ExtractItemResult(app.label, true, result.value.displayName)
                    is AppResult.Failure -> ExtractItemResult(app.label, false, result.error.message ?: "Failed")
                }
            }
            _state.update {
                it.copy(
                    selected = emptySet(),
                    extraction = ExtractionUiState(
                        total = apps.size,
                        completed = apps.size,
                        currentLabel = "",
                        fraction = 1f,
                        results = results.toList(),
                        done = true,
                    ),
                )
            }
        }
    }

    fun dismissExtraction() = _state.update { it.copy(extraction = null) }
}
