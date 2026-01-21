package com.github.mikeandv.pingwatch.entity

sealed class TimeInputResult {
    data class Empty(
        val timeMillis: Long = 0L,
        val unformatted: String = ""
    ) : TimeInputResult()

    data class Partial(
        val unformatted: String,
        val error: String = "Invalid format (MM:SS)"
    ) : TimeInputResult()

    data class Valid(
        val unformatted: String,
        val timeMillis: Long
    ) : TimeInputResult()

    data class Error(
        val message: String
    ) : TimeInputResult()
}
