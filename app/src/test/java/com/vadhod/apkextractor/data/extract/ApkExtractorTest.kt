package com.vadhod.apkextractor.data.extract

import com.vadhod.apkextractor.core.TestDispatchers
import com.vadhod.apkextractor.core.model.AppEntry
import com.vadhod.apkextractor.core.model.ExportFormat
import com.vadhod.apkextractor.core.util.AppError
import com.vadhod.apkextractor.core.util.AppResult
import com.vadhod.apkextractor.core.util.apkFileName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipFile
import kotlin.random.Random

/**
 * Verifies the reconstructed extraction pipeline against a temp-dir [ApkSink] fake (architecture.md
 * §11): byte-exact single copy, valid `.apks` bundle, typed failures, and cancellation re-throw.
 */
class ApkExtractorTest {

    @get:Rule val tmp = TemporaryFolder()

    private val extractor = ApkExtractor(TestDispatchers)

    @Test
    fun single_apk_copy_is_byte_exact_and_reports_full_progress() = runBlocking {
        val bytes = Random.nextBytes(64 * 1024 + 123)
        val source = tmp.newFile("base.apk").apply { writeBytes(bytes) }
        val app = appEntry(base = source.path)
        val outDir = tmp.newFolder("out")
        var lastProgress = -1f

        val result = extractor.extract(app, ExportFormat.SingleBaseApk, TempDirSink(outDir)) {
            lastProgress = it
        }

        assertTrue("expected success, got $result", result is AppResult.Success)
        val outFile = File(outDir, apkFileName(app, "apk"))
        assertArrayEquals(bytes, outFile.readBytes())
        assertEquals(1f, lastProgress, 0f)
        assertEquals(outFile.name, (result as AppResult.Success).value.displayName)
    }

    @Test
    fun bundle_zips_base_and_all_splits() = runBlocking {
        val base = tmp.newFile("base.apk").apply { writeBytes(Random.nextBytes(2048)) }
        val split = File(tmp.root, "split_config.arm64_v8a.apk").apply { writeBytes(Random.nextBytes(1024)) }
        val app = appEntry(base = base.path, splits = listOf(split.path))
        val outDir = tmp.newFolder("out")

        val result = extractor.extract(app, ExportFormat.BundleApks, TempDirSink(outDir)) {}

        assertTrue("expected success, got $result", result is AppResult.Success)
        val outFile = File(outDir, apkFileName(app, "apks"))
        ZipFile(outFile).use { zip ->
            val names = zip.entries().toList().map { it.name }.toSet()
            assertEquals(setOf("base.apk", "split_config.arm64_v8a.apk"), names)
        }
    }

    @Test
    fun missing_base_apk_returns_not_found() = runBlocking {
        val app = appEntry(base = File(tmp.root, "gone.apk").path)
        val result = extractor.extract(app, ExportFormat.SingleBaseApk, TempDirSink(tmp.newFolder())) {}
        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.NotFound)
    }

    @Test
    fun out_of_space_io_error_is_classified() = runBlocking {
        val source = tmp.newFile("base.apk").apply { writeBytes(Random.nextBytes(512)) }
        val app = appEntry(base = source.path)
        val sink = failingSink { throw IOException("No space left on device (ENOSPC)") }

        val result = extractor.extract(app, ExportFormat.SingleBaseApk, sink) {}

        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.OutOfSpace)
    }

    @Test
    fun generic_io_error_maps_to_io_failure() = runBlocking {
        val source = tmp.newFile("base.apk").apply { writeBytes(Random.nextBytes(512)) }
        val app = appEntry(base = source.path)
        val sink = failingSink { throw IOException("permission problem") }

        val result = extractor.extract(app, ExportFormat.SingleBaseApk, sink) {}

        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Io)
    }

    @Test
    fun cancellation_is_rethrown_not_swallowed_into_failure() {
        val source = tmp.newFile("base.apk").apply { writeBytes(Random.nextBytes(2048)) }
        val app = appEntry(base = source.path)
        val sink = failingSink { throw CancellationException("cancelled mid-write") }

        assertThrows(CancellationException::class.java) {
            runBlocking { extractor.extract(app, ExportFormat.SingleBaseApk, sink) {} }
        }
    }

    // --- helpers -------------------------------------------------------------

    private fun appEntry(base: String, splits: List<String> = emptyList()) = AppEntry(
        packageName = "com.example.app",
        label = "Example App",
        versionName = "1.0",
        versionCode = 7,
        isSystem = false,
        baseApkPath = base,
        splitApkPaths = splits,
        totalSizeBytes = 0,
        firstInstallTime = 0,
        lastUpdateTime = 0,
        minSdk = 29,
        targetSdk = 37,
    )

    /** Real sink writing into [dir], so byte-for-byte and zip assertions run against actual files. */
    private class TempDirSink(private val dir: File) : ApkSink {
        override fun create(desiredName: String, mimeType: String): SinkFile {
            val file = File(dir, desiredName)
            return object : SinkFile {
                override val displayName = desiredName
                override fun openOutputStream(): OutputStream = file.outputStream()
                override fun delete() { file.delete() }
            }
        }
    }

    /** Sink whose output stream throws on first write, to exercise failure mapping / cancellation. */
    private fun failingSink(onWrite: () -> Nothing): ApkSink = object : ApkSink {
        override fun create(desiredName: String, mimeType: String): SinkFile = object : SinkFile {
            override val displayName = desiredName
            override fun openOutputStream(): OutputStream = object : OutputStream() {
                override fun write(b: Int) = onWrite()
                override fun write(b: ByteArray, off: Int, len: Int) = onWrite()
            }
            override fun delete() {}
        }
    }
}
