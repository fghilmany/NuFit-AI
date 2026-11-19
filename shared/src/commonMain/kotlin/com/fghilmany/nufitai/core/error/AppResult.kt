package com.fghilmany.nufitai.core.error

import com.fghilmany.nufitai.core.logging.AppLogger
import kotlinx.coroutines.CancellationException

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val failure: Failure) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.fold(onError: (Failure) -> R, onSuccess: (T) -> R): R =
    when (this) {
        is AppResult.Success -> onSuccess(data)
        is AppResult.Error -> onError(failure)
    }

private val databaseLogger = AppLogger.tagged("Database")

// Dev audit (20 Nov 2026): this used to surface the raw caught exception's message straight to
// the UI (e.message is a SQLDelight/SQLite driver message, e.g. "UNIQUE constraint failed:
// meal_preference.id" -- not something a user should ever see). The real message is still
// captured in logs; the user only ever sees a plain, generic message.
inline fun <T> runCatchingDatabase(block: () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        databaseLogger.e(e) { "Database operation failed -- ${e.message}" }
        AppResult.Error(Failure.Database("Something went wrong saving your data. Please try again."))
    }
