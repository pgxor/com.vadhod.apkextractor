package com.vadhod.apkextractor.core.util

import com.vadhod.apkextractor.core.model.AppEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FilenamesTest {

    @Test
    fun sanitize_strips_illegal_characters() {
        val result = sanitizeFileName("""My/App:Name*?"<>|""")
        assertFalse(result.any { it in """/\:*?"<>|""" })
    }

    @Test
    fun sanitize_falls_back_when_blank() {
        assertEquals("fallback", sanitizeFileName("///", fallback = "fallback"))
    }

    @Test
    fun apkFileName_uses_label_version_code() {
        val app = AppEntry(
            packageName = "com.example.app",
            label = "Example App",
            versionName = "2.3.1",
            versionCode = 42,
            isSystem = false,
            baseApkPath = "/x/base.apk",
            splitApkPaths = emptyList(),
            totalSizeBytes = 0,
            firstInstallTime = 0,
            lastUpdateTime = 0,
            minSdk = 29,
            targetSdk = 37,
        )
        assertEquals("Example_App_2.3.1_42.apk", apkFileName(app, "apk"))
    }
}
