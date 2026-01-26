package com.github.mikeandv.pingwatch.result

import com.github.mikeandv.pingwatch.domain.ErrorType

data class RequestTimings(
    val url: String,
    val success: Boolean,
    val statusCode: Int?,
    val error: String?,
    val errorType: ErrorType = ErrorType.NONE,
    val callMs: Double,
    val dnsMs: Double?,
    val connectMs: Double?,
    val tlsMs: Double?,
    val requestHeadersMs: Double?,
    val requestBodyMs: Double?,
    val responseHeadersMs: Double?,
    val responseBodyMs: Double?
)