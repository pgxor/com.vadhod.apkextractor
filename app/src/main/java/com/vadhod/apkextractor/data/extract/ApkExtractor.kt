package com.vadhod.apkextractor.data.extract

import com.vadhod.apkextractor.core.model.AppEntry
import com.vadhod.apkextractor.core.model.ExportFormat
import com.vadhod.apkextractor.core.model.ExtractOutcome
import com.vadhod.apkextractor.core.util.AppError
import com.vadhod.apkextractor.core.util.AppResult
import com.vadhod.apkextractor.core.util.DispatcherProvider
import com.vadhod.apkextractor.core.util.apkFileName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.coroutineContext

/**
 * The extraction pipeline — the part that must be bulletproof (architecture.md §7.2, rules.md §C-18).
 *
 * - [ExportFormat.SingleBaseApk] stream-copies `base.apk`.
 * - [ExportFormat.BundleApks] zips base + every split into a reinstallable `.apks` archive.
 *
 * All I/O runs on [DispatcherProvider.io], is buffered, reports progress, and is cancellable. Nothing
 * ever throws to the caller: failures come back as [AppResult.Failure] with a typed [AppError]; a
 * partially-written file is deleted so no corrupt output survives.
 */
class ApkExtractor(private val dispatchers: DispatcherProvider) {

    suspend fun extract(
        app: AppEntry,
        format: ExportFormat,
        sink: ApkSink,
        onProgress: (Float) -> Unit,
    ): AppResult<ExtractOutcome> = withContext(dispatchers.io) {
        try {
            val outcome = when (format) {
                ExportFormat.SingleBaseApk -> extractSingle(app, sink, onProgress)
                ExportFormat.BundleApks -> extractBundle(app, sink, onProgress)
            }
            AppResult.Success(outcome)
        } catch (e: CancellationException) {
            throw e // keep structured cancellation working (rules.md §C-15)
        } catch (e: Throwable) {
            AppResult.Failure(e.toAppError())
        }
    }

    private suspend fun extractSingle(
        app: AppEntry,
        sink: ApkSink,
        onProgress: (Float) -> Unit,
    ): ExtractOutcome {
        val source = File(app.baseApkPath)
        if (!source.exists()) throw FileNotFoundException("base.apk no longer exists for ${app.packageName}")

        val file = sink.create(apkFileName(app, "apk"), APK_MIME)
        var written = 0L
        try {
            val total = source.length().coerceAtLeast(1L)
            file.openOutputStream().use { out ->
                source.inputStream().use { input ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        coroutineContext.ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        out.write(buffer, 0, read)
                        written += read
                        onProgress((written.toFloat() / total).coerceIn(0f, 1f))
                    }
                    out.flush()
                }
            }
        } catch (e: Throwable) {
            file.delete()
            throw e
        }
        onProgress(1f)
        return ExtractOutcome(displayName = file.displayName, bytesWritten = written)
    }

    private suspend fun extractBundle(
        app: AppEntry,
        sink: ApkSink,
        onProgress: (Float) -> Unit,
    ): ExtractOutcome {
        val parts = app.allApkPaths.map(::File)
        parts.firstOrNull { !it.exists() }?.let {
            throw FileNotFoundException("APK part missing: ${it.name}")
        }

        val file = sink.create(apkFileName(app, "apks"), APK_MIME)
        val grandTotal = parts.sumOf { it.length() }.coerceAtLeast(1L)
        var written = 0L
        try {
            file.openOutputStream().use { rawOut ->
                ZipOutputStream(rawOut.buffered()).use { zip ->
                    for (part in parts) {
                        coroutineContext.ensureActive()
                        zip.putNextEntry(ZipEntry(part.name))
                        part.inputStream().use { input ->
                            val buffer = ByteArray(BUFFER_SIZE)
                            while (true) {
                                coroutineContext.ensureActive()
                                val read = input.read(buffer)
                                if (read < 0) break
                                zip.write(buffer, 0, read)
                                written += read
                                onProgress((written.toFloat() / grandTotal).coerceIn(0f, 1f))
                            }
                        }
                        zip.closeEntry()
                    }
                }
            }
        } catch (e: Throwable) {
            file.delete()
            throw e
        }
        onProgress(1f)
        return ExtractOutcome(displayName = file.displayName, bytesWritten = written)
    }

    private fun Throwable.toAppError(): AppError = when {
        this is FileNotFoundException -> AppError.NotFound(message)
        this is SecurityException -> AppError.PermissionDenied(message)
        this is IOException && message?.contains("space", ignoreCase = true) == true ->
            AppError.OutOfSpace(message)
        this is IOException && message?.contains("ENOSPC", ignoreCase = true) == true ->
            AppError.OutOfSpace(message)
        this is IOException -> AppError.Io(message, this)
        else -> AppError.Unknown(message, this)
    }

    private companion object {
        const val APK_MIME = "application/vnd.android.package-archive"
        const val BUFFER_SIZE = 64 * 1024
    }
}
