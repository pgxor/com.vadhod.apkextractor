package com.vadhod.apkextractor.data.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.vadhod.apkextractor.core.model.AppEntry
import com.vadhod.apkextractor.core.util.apkFileName
import com.vadhod.apkextractor.core.util.sanitizeFileName
import java.io.File

/**
 * One-off share / icon-export helpers (architecture.md §7.3–7.4). Sharing copies `base.apk` into the
 * only FileProvider-exposed location (`cacheDir/shared/`, see res/xml/file_paths.xml) before granting
 * a content URI — the real APK path under /data/app is not directly shareable.
 */
object Exporters {

    private const val APK_MIME = "application/vnd.android.package-archive"

    fun iconFileName(app: AppEntry): String =
        "${sanitizeFileName(app.label, app.packageName)}_icon.png"

    /** Renders the app's launcher icon to a PNG at the user-chosen SAF [uri]. Returns success. */
    fun exportIconPng(context: Context, app: AppEntry, uri: Uri): Boolean = runCatching {
        val bitmap: Bitmap = context.packageManager.getApplicationIcon(app.packageName).toBitmap()
        context.contentResolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        } ?: return false
        true
    }.getOrDefault(false)

    /** Builds an ACTION_SEND intent for the app's base APK via a FileProvider content URI. */
    fun buildApkShareIntent(context: Context, app: AppEntry): Intent {
        val shareDir = File(context.cacheDir, "shared").apply { mkdirs() }
        val staged = File(shareDir, apkFileName(app, "apk"))
        File(app.baseApkPath).copyTo(staged, overwrite = true)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", staged)
        return Intent(Intent.ACTION_SEND).apply {
            type = APK_MIME
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
