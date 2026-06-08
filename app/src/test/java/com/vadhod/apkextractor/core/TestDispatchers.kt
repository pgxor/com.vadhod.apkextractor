package com.vadhod.apkextractor.core

import com.vadhod.apkextractor.core.util.DispatcherProvider
import kotlinx.coroutines.Dispatchers

/** Deterministic dispatchers for unit tests (everything runs unconfined / inline). */
object TestDispatchers : DispatcherProvider {
    override val main = Dispatchers.Unconfined
    override val default = Dispatchers.Unconfined
    override val io = Dispatchers.Unconfined
}
