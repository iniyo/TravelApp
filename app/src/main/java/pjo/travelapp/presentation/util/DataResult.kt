package pjo.travelapp.presentation.util

sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Error(val exception: Throwable) : DataResult<Nothing>()
    object Loading : DataResult<Nothing>()
}