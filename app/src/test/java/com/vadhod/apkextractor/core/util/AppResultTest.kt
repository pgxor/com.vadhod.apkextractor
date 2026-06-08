package com.vadhod.apkextractor.core.util

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AppResultTest {

    @Test
    fun appResult_wraps_success() {
        val result = appResult { 21 + 8 }
        assertTrue(result is AppResult.Success)
        assertEquals(29, (result as AppResult.Success).value)
    }

    @Test
    fun appResult_maps_throwable_to_failure() {
        val result = appResult { error("boom") }
        assertTrue(result is AppResult.Failure)
        assertEquals("boom", (result as AppResult.Failure).error.message)
    }

    @Test
    fun appResult_rethrows_cancellation() {
        try {
            appResult { throw CancellationException("cancelled") }
            fail("CancellationException must propagate, not be wrapped")
        } catch (e: CancellationException) {
            // expected
        }
    }
}
