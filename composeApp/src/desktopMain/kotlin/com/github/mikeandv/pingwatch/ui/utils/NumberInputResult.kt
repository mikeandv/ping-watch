package com.github.mikeandv.pingwatch.ui.utils

sealed class NumberInputResult<out T : Number> {
    data class Empty<T : Number>(val value: T) : NumberInputResult<T>()
    data class Valid<T : Number>(val value: T) : NumberInputResult<T>()
    data class Error<T : Number>(val message: String) : NumberInputResult<T>()
}
