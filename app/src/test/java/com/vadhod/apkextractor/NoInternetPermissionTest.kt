package com.vadhod.apkextractor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * GUARDRAIL — do not delete or weaken.
 *
 * This app is OFFLINE-ONLY and privacy-first (rules.md §A, architecture.md §9). It must NEVER
 * request the INTERNET permission. If this test fails, a network capability was introduced — stop
 * and remove it rather than editing this test.
 *
 * Note: this checks the source manifest. A merged-manifest / instrumented check (catching any
 * library that injects INTERNET) is tracked as a follow-up to tasks.md T-030/T-064.
 */
class NoInternetPermissionTest {

    @Test
    fun manifest_does_not_request_network_permissions() {
        val manifest = locateMainManifest()
        assertTrue("Could not locate src/main/AndroidManifest.xml (working dir: ${File("").absolutePath})", manifest.exists())
        val text = manifest.readText()
        assertFalse(
            "App must not declare android.permission.INTERNET (offline-only).",
            text.contains("android.permission.INTERNET"),
        )
        assertFalse(
            "App must not declare android.permission.ACCESS_NETWORK_STATE (offline-only).",
            text.contains("android.permission.ACCESS_NETWORK_STATE"),
        )
    }

    private fun locateMainManifest(): File {
        listOf(
            File("src/main/AndroidManifest.xml"),
            File("app/src/main/AndroidManifest.xml"),
        ).firstOrNull { it.exists() }?.let { return it }

        var dir: File? = File("").absoluteFile
        while (dir != null) {
            File(dir, "app/src/main/AndroidManifest.xml").takeIf { it.exists() }?.let { return it }
            File(dir, "src/main/AndroidManifest.xml").takeIf { it.exists() }?.let { return it }
            dir = dir.parentFile
        }
        return File("src/main/AndroidManifest.xml")
    }
}
