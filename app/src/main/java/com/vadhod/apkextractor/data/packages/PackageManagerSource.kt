package com.vadhod.apkextractor.data.packages

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.vadhod.apkextractor.core.model.AppEntry
import com.vadhod.apkextractor.core.util.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Reads installed apps from [PackageManager] and maps them to the pure-domain [AppEntry]
 * (architecture.md §7.1). All work runs on [DispatcherProvider.io] — enumeration + label loading is
 * blocking (rules.md §C-15). No Android types escape past this boundary.
 */
class PackageManagerSource(
    private val packageManager: PackageManager,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun loadAll(): List<AppEntry> = withContext(dispatchers.io) {
        @Suppress("DEPRECATION", "QueryPermissionsNeeded")
        packageManager.getInstalledPackages(0).mapNotNull { it.toAppEntry() }
    }

    private fun PackageInfo.toAppEntry(): AppEntry? {
        val ai = applicationInfo ?: return null
        val base = ai.sourceDir ?: return null
        val splits = ai.splitSourceDirs?.toList().orEmpty()
        val totalSize = (listOf(base) + splits).sumOf { path ->
            runCatching { File(path).length() }.getOrDefault(0L)
        }
        return AppEntry(
            packageName = packageName,
            label = ai.loadLabel(packageManager).toString(),
            versionName = versionName,
            versionCode = longVersionCode,
            isSystem = (ai.flags and SYSTEM_FLAGS) != 0,
            baseApkPath = base,
            splitApkPaths = splits,
            totalSizeBytes = totalSize,
            firstInstallTime = firstInstallTime,
            lastUpdateTime = lastUpdateTime,
            minSdk = ai.minSdkVersion,
            targetSdk = ai.targetSdkVersion,
        )
    }

    private companion object {
        const val SYSTEM_FLAGS = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
    }
}
