package com.vadhod.apkextractor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vadhod.apkextractor.core.design.theme.VadhodTheme
import com.vadhod.apkextractor.core.model.AppEntry
import com.vadhod.apkextractor.core.model.ExportFormat
import com.vadhod.apkextractor.data.settings.Settings
import com.vadhod.apkextractor.data.share.Exporters
import com.vadhod.apkextractor.feature.applist.AppListScreen
import com.vadhod.apkextractor.feature.applist.AppListViewModel
import com.vadhod.apkextractor.feature.applist.AppTab
import com.vadhod.apkextractor.feature.detail.AppDetailScreen
import com.vadhod.apkextractor.feature.settings.SettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class Route { LIST, DETAIL, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as App).container
        setContent {
            val settings by container.settingsRepository.settings.collectAsState(initial = Settings())
            VadhodTheme(themeMode = settings.themeMode) {
                AppRoot(container = container, settings = settings)
            }
        }
    }
}

@Composable
private fun AppRoot(container: AppContainer, settings: Settings) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val vm: AppListViewModel = viewModel(
        factory = viewModelFactory {
            initializer { AppListViewModel(context.applicationContext, container) }
        },
    )
    val state by vm.state.collectAsState()

    var route by rememberSaveable { mutableStateOf(Route.LIST) }
    var detailPackage by rememberSaveable { mutableStateOf<String?>(null) }

    var pendingExtract by remember { mutableStateOf<Pair<List<AppEntry>, ExportFormat?>?>(null) }
    var pendingIconApp by remember { mutableStateOf<AppEntry?>(null) }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        val pending = pendingExtract
        pendingExtract = null
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
            scope.launch { container.settingsRepository.setExportTreeUri(uri.toString()) }
            pending?.let { (apps, fmt) -> vm.extract(apps, uri, fmt) }
        }
    }

    val iconPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png"),
    ) { uri ->
        val app = pendingIconApp
        pendingIconApp = null
        if (uri != null && app != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { Exporters.exportIconPng(context, app, uri) }
                Toast.makeText(context, if (ok) "Icon exported" else "Icon export failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun ensureFolderAndExtract(apps: List<AppEntry>, format: ExportFormat?) {
        if (apps.isEmpty()) return
        val tree = settings.exportTreeUri
        if (tree != null) {
            vm.extract(apps, Uri.parse(tree), format)
        } else {
            pendingExtract = apps to format
            folderPicker.launch(null)
        }
    }

    fun shareApk(app: AppEntry) {
        scope.launch {
            runCatching {
                val intent = withContext(Dispatchers.IO) { Exporters.buildApkShareIntent(context, app) }
                context.startActivity(Intent.createChooser(intent, "Share APK"))
            }.onFailure {
                Toast.makeText(context, "Share failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BackHandler(enabled = route != Route.LIST || state.selectionMode) {
        when {
            state.selectionMode -> vm.clearSelection()
            else -> {
                route = Route.LIST
                detailPackage = null
            }
        }
    }

    when (route) {
        Route.SETTINGS -> {
            val exportLabel = settings.exportTreeUri?.let {
                runCatching { DocumentFile.fromTreeUri(context, Uri.parse(it))?.name }.getOrNull()
            } ?: "Not set — you'll be asked when extracting"
            SettingsScreen(
                settings = settings,
                exportFolderLabel = exportLabel,
                onBack = { route = Route.LIST },
                onThemeMode = { scope.launch { container.settingsRepository.setThemeMode(it) } },
                onBundleToggle = { scope.launch { container.settingsRepository.setBundleSplitsByDefault(it) } },
                onPickFolder = { pendingExtract = null; folderPicker.launch(null) },
            )
        }

        Route.DETAIL -> {
            val app = remember(detailPackage, state.userApps, state.systemApps) {
                (state.userApps + state.systemApps).firstOrNull { it.packageName == detailPackage }
            }
            if (app == null) {
                route = Route.LIST
            } else {
                AppDetailScreen(
                    app = app,
                    container = container,
                    onBack = { route = Route.LIST; detailPackage = null },
                    onExtract = { a, fmt -> ensureFolderAndExtract(listOf(a), fmt) },
                    onShare = { shareApk(it) },
                    onExportIcon = { a -> pendingIconApp = a; iconPicker.launch(Exporters.iconFileName(a)) },
                )
            }
        }

        Route.LIST -> AppListScreen(
            state = state,
            onQueryChange = vm::onQueryChange,
            onTabSelected = vm::onTabSelected,
            onSortSelected = vm::onSortSelected,
            onToggleSelect = vm::toggleSelection,
            onClearSelection = vm::clearSelection,
            onSelectAllVisible = vm::selectAllVisible,
            onOpenApp = { detailPackage = it.packageName; route = Route.DETAIL },
            onOpenSettings = { route = Route.SETTINGS },
            onExtractSelected = { apps, fmt -> ensureFolderAndExtract(apps, fmt) },
            appsForSelection = vm::appsForSelection,
            onDismissExtraction = vm::dismissExtraction,
        )
    }
}
