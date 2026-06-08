package com.vadhod.apkextractor.core.model

/**
 * How an app should be exported.
 *
 * - [SingleBaseApk]: copy only `base.apk` (clean single file; may not reinstall for split apps).
 * - [BundleApks]: zip base + all splits into one reinstallable `.apks` archive (default for split
 *   apps, questionnaire Q9/Q10).
 */
sealed interface ExportFormat {
    data object SingleBaseApk : ExportFormat
    data object BundleApks : ExportFormat
}

/** Result of a single successful extraction. */
data class ExtractOutcome(
    val displayName: String,
    val bytesWritten: Long,
)

/** Progress callback payload for long extractions (0f..1f, plus the app being processed). */
data class ExtractProgress(
    val packageName: String,
    val fraction: Float,
)
