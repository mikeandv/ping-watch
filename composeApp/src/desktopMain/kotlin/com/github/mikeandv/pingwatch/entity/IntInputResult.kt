package com.github.mikeandv.pingwatch.entity

sealed class IntInputResult {
    data class Valid(val value: Int) : IntInputResult()
    data class Error(val message: String) : IntInputResult()
}
