package com.vadhod.apkextractor.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Abstraction over coroutine dispatchers.
 *
 * All PackageManager / file / SAF work must run off the main thread (see rules.md §C-15). Injecting
 * this interface lets the data layer pick [io] for blocking I/O and lets tests substitute a
 * deterministic dispatcher (e.g. a `StandardTestDispatcher`).
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
}

/** Production implementation backed by [Dispatchers]. */
object DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val default: CoroutineDispatcher get() = Dispatchers.Default
    override val io: CoroutineDispatcher get() = Dispatchers.IO
}
