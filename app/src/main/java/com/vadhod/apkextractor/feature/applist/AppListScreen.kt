package com.vadhod.apkextractor.feature.applist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vadhod.apkextractor.core.design.components.AppListItem
import com.vadhod.apkextractor.core.design.components.GradientBackground
import com.vadhod.apkextractor.core.design.components.LoadingState
import com.vadhod.apkextractor.core.design.components.MessageState
import com.vadhod.apkextractor.core.design.components.PillTab
import com.vadhod.apkextractor.core.design.components.PillTabs
import com.vadhod.apkextractor.core.design.components.SearchField
import com.vadhod.apkextractor.core.model.AppEntry
import com.vadhod.apkextractor.core.model.ExportFormat
import com.vadhod.apkextractor.core.model.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    state: AppListUiState,
    onQueryChange: (String) -> Unit,
    onTabSelected: (AppTab) -> Unit,
    onSortSelected: (SortOrder) -> Unit,
    onToggleSelect: (String) -> Unit,
    onClearSelection: () -> Unit,
    onSelectAllVisible: () -> Unit,
    onOpenApp: (AppEntry) -> Unit,
    onOpenSettings: () -> Unit,
    onExtractSelected: (List<AppEntry>, ExportFormat?) -> Unit,
    appsForSelection: () -> List<AppEntry>,
    onDismissExtraction: () -> Unit,
) {
    val visible = state.visibleApps
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppListTopBar(
                state = state,
                onSortSelected = onSortSelected,
                onOpenSettings = onOpenSettings,
                onClearSelection = onClearSelection,
                onSelectAllVisible = onSelectAllVisible,
            )
        },
        floatingActionButton = {
            if (state.selectionMode) {
                ExtendedFloatingActionButton(
                    onClick = { onExtractSelected(appsForSelection(), null) },
                    icon = { Icon(Icons.Rounded.Download, contentDescription = null) },
                    text = { Text("Extract ${state.selected.size}", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
    ) { padding ->
        GradientBackground {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                PillTabs(
                    tabs = listOf(
                        PillTab("User", state.userApps.size),
                        PillTab("System", state.systemApps.size),
                    ),
                    selectedIndex = if (state.tab == AppTab.USER) 0 else 1,
                    onSelect = { onTabSelected(if (it == 0) AppTab.USER else AppTab.SYSTEM) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                SearchField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    placeholder = "Search apps or packages",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )

                when {
                    state.loading -> LoadingState()
                    visible.isEmpty() -> MessageState(
                        icon = if (state.query.isBlank()) Icons.Rounded.Apps else Icons.Rounded.SearchOff,
                        title = if (state.query.isBlank()) "No apps here" else "No matches",
                        subtitle = if (state.query.isBlank()) null else "Try a different search term.",
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(visible, key = { it.packageName }) { app ->
                            AppListItem(
                                app = app,
                                selected = app.packageName in state.selected,
                                selectionMode = state.selectionMode,
                                onClick = {
                                    if (state.selectionMode) onToggleSelect(app.packageName) else onOpenApp(app)
                                },
                                onLongClick = { onToggleSelect(app.packageName) },
                            )
                        }
                    }
                }
            }
        }
    }

    state.extraction?.let { extraction ->
        ExtractionSheet(state = extraction, onDismiss = onDismissExtraction)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppListTopBar(
    state: AppListUiState,
    onSortSelected: (SortOrder) -> Unit,
    onOpenSettings: () -> Unit,
    onClearSelection: () -> Unit,
    onSelectAllVisible: () -> Unit,
) {
    if (state.selectionMode) {
        TopAppBar(
            title = { Text("${state.selected.size} selected", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear selection")
                }
            },
            actions = {
                IconButton(onClick = onSelectAllVisible) {
                    Icon(Icons.Rounded.DoneAll, contentDescription = "Select all visible")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
    } else {
        TopAppBar(
            title = { Text("Vadhod APK Extractor", fontWeight = FontWeight.ExtraBold) },
            actions = {
                SortMenu(current = state.sortOrder, onSortSelected = onSortSelected)
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun SortMenu(current: SortOrder, onSortSelected: (SortOrder) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        sortLabels.forEach { (order, label) ->
            DropdownMenuItem(
                text = {
                    Text(
                        label,
                        fontWeight = if (order == current) FontWeight.Bold else FontWeight.Normal,
                        color = if (order == current) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                },
                onClick = {
                    onSortSelected(order)
                    expanded = false
                },
            )
        }
    }
}

private val sortLabels = listOf(
    SortOrder.NAME_ASC to "Name (A–Z)",
    SortOrder.NAME_DESC to "Name (Z–A)",
    SortOrder.SIZE_DESC to "Size (largest)",
    SortOrder.SIZE_ASC to "Size (smallest)",
    SortOrder.INSTALL_NEWEST to "Recently installed",
    SortOrder.UPDATED_NEWEST to "Recently updated",
)
