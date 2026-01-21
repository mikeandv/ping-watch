package com.github.mikeandv.pingwatch.entity

sealed class CountInputResult {
    data class Empty(val value: Long = 0L) : CountInputResult()
    data class Valid(val value: Long) : CountInputResult()
    data class Error(val message: String) : CountInputResult()
}

