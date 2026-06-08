package com.vadhod.apkextractor.core.util

import kotlinx.coroutines.CancellationException

/**
 * Lightweight success/failure result for fallible operations (extraction, file/SAF I/O).
 *
 * Keeps errors typed and prevents exceptions from leaking to the UI — operations like extraction
 * must never crash the app, they return a [Failure] instead (see rules.md §C-18, architecture.md §7.2).
 */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

/**
 * Domain error taxonomy. Extend as new failure modes appear; map platform exceptions into one of
 * these at the data-layer boundary so the UI can show meaningful, localized messages.
 */
sealed class AppError(open val message: String?, open val cause: Throwable? = null) {
    data class Io(override val message: String?, override val cause: Throwable? = null) : AppError(message, cause)
    data class NotFound(override val message: String?) : AppError(message)
    data class PermissionDenied(override val message: String?) : AppError(message)
    data class OutOfSpace(override val message: String?) : AppError(message)
    data class Cancelled(override val message: String? = "Cancelled") : AppError(message)
    data class Unknown(override val message: String?, override val cause: Throwable? = null) : AppError(message, cause)
}

/**
 * Runs [block], returning [AppResult.Success] or mapping any throwable to [AppResult.Failure].
 *
 * Coroutine [CancellationException] is always re-thrown so structured concurrency / cancellation
 * keeps working (rules.md §C-15).
 */
inline fun <T> appResult(block: () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        AppResult.Failure(AppError.Unknown(e.message, e))
    }
