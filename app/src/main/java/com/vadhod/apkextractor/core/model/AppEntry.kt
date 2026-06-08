package com.vadhod.apkextractor.core.model

/**
 * An installed application as the app cares about it. Pure domain type — no Android framework types
 * leak in (rules.md §C-13). Built off `PackageManager` data by the data layer.
 *
 * Sizes are in bytes. [splitApkPaths] is empty for simple (non-split) apps.
 */
data class AppEntry(
    val packageName: String,
    val label: String,
    val versionName: String?,
    val versionCode: Long,
    val isSystem: Boolean,
    val baseApkPath: String,
    val splitApkPaths: List<String>,
    val totalSizeBytes: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val minSdk: Int?,
    val targetSdk: Int?,
) {
    /** True when the app ships as split APKs (base + config splits) and needs bundling to reinstall. */
    val isSplit: Boolean get() = splitApkPaths.isNotEmpty()

    /** All on-disk APK paths (base first), in a stable order. */
    val allApkPaths: List<String> get() = buildList {
        add(baseApkPath)
        addAll(splitApkPaths)
    }
}
