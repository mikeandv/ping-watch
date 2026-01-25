package com.github.mikeandv.pingwatch.result

data class UrlAvgMetrics(
    val url: String,
    val count: Int,
    val ok: Int,
    val fail: Int,

    val callAvgMs: Double,

    val dnsAvgMs: Double?,
    val connectAvgMs: Double?,
    val tlsAvgMs: Double?,
    val requestHeadersAvgMs: Double?,
    val requestBodyAvgMs: Double?,
    val responseHeadersAvgMs: Double?,
    val responseBodyAvgMs: Double?
)