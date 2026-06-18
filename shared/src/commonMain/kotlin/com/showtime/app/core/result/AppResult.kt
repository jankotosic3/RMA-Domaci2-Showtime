package com.showtime.app.core.result

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String) : AppResult<Nothing>
}
