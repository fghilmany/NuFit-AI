package com.fghilmany.nufitai.core.error

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

inline fun <T> runCatchingDatabase(block: () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Error(Failure.Database(e.message ?: "Unknown database error"))
    }
