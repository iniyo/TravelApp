package pjo.travelapp.presentation.util

sealed class LatestUiState<out T> {
    data class Success<out T>(val data: T) : LatestUiState<T>()
    data class Error(val exception: Throwable) : LatestUiState<Nothing>()
    data object Loading : LatestUiState<Nothing>()
}