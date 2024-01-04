package ui.model

sealed interface ModelState<T> {
    class Init<T> : ModelState<T>
    data class Success<T>(val result: T) : ModelState<T>
    data class Error<T>(val msg: String) : ModelState<T>
}