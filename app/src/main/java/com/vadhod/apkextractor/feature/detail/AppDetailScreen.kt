package com.vadhod.apkextractor.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vadhod.apkextractor.AppContainer
import com.vadhod.apkextractor.core.design.components.AppIcon
import com.vadhod.apkextractor.core.design.components.GradientBackground
import com.vadhod.apkextractor.core.model.AppEntry
import com.vadhod.apkextractor.core.model.ExportFormat
import com.vadhod.apkextractor.core.util.formatBytes
import com.vadhod.apkextractor.core.util.formatDate
import com.vadhod.apkextractor.data.inspect.ApkEntryInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    app: AppEntry,
    container: AppContainer,
    onBack: () -> Unit,
    onExtract: (AppEntry, ExportFormat?) -> Unit,
    onShare: (AppEntry) -> Unit,
    onExportIcon: (AppEntry) -> Unit,
) {
    val signing by produceState<List<String>?>(initialValue = null, app.packageName) {
        value = runCatching { container.apkInspector.signingSha256(app.packageName) }.getOrDefault(emptyList())
    }
    val entries by produceState<List<ApkEntryInfo>?>(initialValue = null, app.baseApkPath) {
        value = runCatching { container.apkInspector.listEntries(app.baseApkPath) }.getOrDefault(emptyList())
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(app.label, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        GradientBackground {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { HeroCard(app) }
                item { ActionButtons(app, onExtract, onShare, onExportIcon) }
                item { InfoCard(app) }
                item { SigningCard(signing) }
                item {
                    SectionHeader(
                        icon = Icons.Rounded.Inventory2,
                        title = "Contents",
                        trailing = entries?.size?.let { "$it files" } ?: "…",
                    )
                }
                val list = entries.orEmpty()
                items(list.take(MAX_ENTRIES)) { entry -> EntryRow(entry) }
                if (list.size > MAX_ENTRIES) {
                    item {
                        Text(
                            "+ ${list.size - MAX_ENTRIES} more files",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
            }
        }
    }
}

private const val MAX_ENTRIES = 400

@Composable
private fun HeroCard(app: AppEntry) {
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppIcon(app = app, size = 64.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(app.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "v${app.versionName ?: "?"} (${app.versionCode}) · ${formatBytes(app.totalSizeBytes)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    app: AppEntry,
    onExtract: (AppEntry, ExportFormat?) -> Unit,
    onShare: (AppEntry) -> Unit,
    onExportIcon: (AppEntry) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { onExtract(app, ExportFormat.SingleBaseApk) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(if (app.isSplit) "Extract base APK" else "Extract APK", fontWeight = FontWeight.Bold)
        }
        if (app.isSplit) {
            FilledTonalButton(onClick = { onExtract(app, ExportFormat.BundleApks) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Inventory2, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Extract bundle (.apks)", fontWeight = FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { onShare(app) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Share")
            }
            OutlinedButton(onClick = { onExportIcon(app) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Icon")
            }
        }
    }
}

@Composable
private fun InfoCard(app: AppEntry) {
    Card {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoRow("Type", if (app.isSystem) "System app" else "User app")
            InfoRow("Version", "${app.versionName ?: "?"} (${app.versionCode})")
            InfoRow("Size", formatBytes(app.totalSizeBytes))
            if (app.isSplit) InfoRow("Splits", "${app.splitApkPaths.size} config split(s)")
            InfoRow("Min SDK", app.minSdk?.toString() ?: "—")
            InfoRow("Target SDK", app.targetSdk?.toString() ?: "—")
            InfoRow("Installed", formatDate(app.firstInstallTime))
            InfoRow("Updated", formatDate(app.lastUpdateTime))
        }
    }
}

@Composable
private fun SigningCard(signing: List<String>?) {
    Card {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Signing (SHA-256)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            when {
                signing == null -> Text("Reading…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                signing.isEmpty() -> Text("Not available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> signing.forEach {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, trailing: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.weight(1f))
        Text(trailing, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EntryRow(entry: ApkEntryInfo) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            entry.name,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            formatBytes(entry.size),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) { content() }
}
