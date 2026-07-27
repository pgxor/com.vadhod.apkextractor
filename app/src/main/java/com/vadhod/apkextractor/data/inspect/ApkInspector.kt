package com.vadhod.apkextractor.data.inspect

import android.content.pm.PackageManager
import com.vadhod.apkextractor.core.util.DispatcherProvider
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.zip.ZipFile

/** One entry inside an APK's zip (used by the detail screen's "Contents" list). */
data class ApkEntryInfo(val name: String, val size: Long)

/**
 * Read-only APK inspection (architecture.md §7.3): signing SHA-256 fingerprints via [PackageManager]
 * and the base APK's zip entries via [ZipFile]. No third-party libraries — plain platform APIs
 * (rules.md §B-12). All work runs on [DispatcherProvider.io].
 */
class ApkInspector(
    private val packageManager: PackageManager,
    private val dispatchers: DispatcherProvider,
) {
    /** SHA-256 fingerprints of the app's signing certificates, formatted as colon-separated hex. */
    suspend fun signingSha256(packageName: String): List<String> = withContext(dispatchers.io) {
        @Suppress("DEPRECATION")
        val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        val signingInfo = info.signingInfo ?: return@withContext emptyList()
        val signatures = if (signingInfo.hasMultipleSigners()) {
            signingInfo.apkContentsSigners
        } else {
            signingInfo.signingCertificateHistory
        }
        signatures.orEmpty().map { signature ->
            MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
                .joinToString(":") { "%02X".format(it) }
        }
    }

    /** Lists the zip entries of [baseApkPath], sorted by name. */
    suspend fun listEntries(baseApkPath: String): List<ApkEntryInfo> = withContext(dispatchers.io) {
        ZipFile(baseApkPath).use { zip ->
            zip.entries().asSequence()
                .map { ApkEntryInfo(it.name, it.size.coerceAtLeast(0L)) }
                .sortedBy { it.name }
                .toList()
        }
    }
}
