package com.github.mikeandv.pingwatch.result

data class RequestTimings(
    val url: String,
    val success: Boolean,
    val statusCode: Int?,
    val error: String?,
    val callMs: Long,
    val dnsMs: Long?,
    val connectMs: Long?,
    val tlsMs: Long?,
    val requestHeadersMs: Long?,
    val requestBodyMs: Long?,
    val responseHeadersMs: Long?,
    val responseBodyMs: Long?
)