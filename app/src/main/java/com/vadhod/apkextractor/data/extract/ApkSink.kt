package com.vadhod.apkextractor.data.extract

import java.io.OutputStream

/**
 * Abstraction over *where* an extracted APK is written. Production uses [SafApkSink] (Storage Access
 * Framework); tests can supply a temp-dir sink. Keeping this an interface lets [ApkExtractor] stay
 * free of Android/SAF types and be unit-tested (architecture.md §11).
 */
interface ApkSink {
    /**
     * Creates a new file to write into, resolving name collisions. [mimeType] is advisory (used by
     * SAF). Throws on failure so the extractor can map it to a typed [com.vadhod.apkextractor.core.util.AppError].
     */
    fun create(desiredName: String, mimeType: String): SinkFile
}

/** A single created output file. */
interface SinkFile {
    /** The final name actually used (may differ from the requested one after collision resolution). */
    val displayName: String

    fun openOutputStream(): OutputStream

    /** Removes a partially-written file after a failure, so no corrupt output is left behind. */
    fun delete()
}
