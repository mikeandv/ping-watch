package com.github.mikeandv.pingwatch.result

import com.github.mikeandv.pingwatch.domain.ErrorType

data class RequestTimings(
    val url: String,
    val success: Boolean,
    val statusCode: Int?,
    val error: String?,
    val errorType: ErrorType = ErrorType.NONE,
    val callMs: Long,
    val dnsMs: Long?,
    val connectMs: Long?,
    val tlsMs: Long?,
    val requestHeadersMs: Long?,
    val requestBodyMs: Long?,
    val responseHeadersMs: Long?,
    val responseBodyMs: Long?
)