package com.github.mikeandv.pingwatch.result

import com.github.mikeandv.pingwatch.domain.ErrorType

class TestCaseResult private constructor(
    val url: String,
    val totalRequestCount: Int,
    val successRequestCount: Int,
    val errorRequestCount: Int,
    val errorsByType: Map<ErrorType, Int>,
    val duration: MetricStatistics,
    val dns: MetricStatistics?,
    val connect: MetricStatistics?,
    val tls: MetricStatistics?,
    val requestHeaders: MetricStatistics?,
    val requestBody: MetricStatistics?,
    val responseHeaders: MetricStatistics?,
    val responseBody: MetricStatistics?
) {
    // Convenience accessors for duration metrics (backward compatibility)
    val min: Double get() = duration.min
    val max: Double get() = duration.max
    val avg: Double get() = duration.avg
    val median: Double get() = duration.median
    val p95: Double get() = duration.p95
    val p99: Double get() = duration.p99

    companion object {
        fun create(timings: List<RequestTimings>): List<TestCaseResult> {
            val resultCalc = mutableListOf<TestCaseResult>()

            val groupedByUrl = timings.groupBy { it.url }

            for ((url, urlTimings) in groupedByUrl) {
                resultCalc.add(createInstance(url, urlTimings))
            }

            return resultCalc.toList()
        }

        private fun createInstance(url: String, timings: List<RequestTimings>): TestCaseResult {
            val totalRequestCountCalc = timings.size

            // Success = status code 200-399
            val successTimings = timings.filter { it.success && it.statusCode != null && it.statusCode in 200..399 }
            val successDurations = successTimings.map { it.callMs }

            val successRequestCountCalc = successDurations.size
            val errorRequestCountCalc = totalRequestCountCalc - successRequestCountCalc

            // Error breakdown by type
            val errorsByType = timings.filter { it.errorType != ErrorType.NONE }
                .groupBy { it.errorType }
                .mapValues { it.value.size }

            // Duration statistics from successful requests
            val durationStats = MetricStatistics.fromValues(successDurations) ?: MetricStatistics.EMPTY

            // Network breakdown statistics from all timings
            val dnsStats = MetricStatistics.fromValues(timings.mapNotNull { it.dnsMs })
            val connectStats = MetricStatistics.fromValues(timings.mapNotNull { it.connectMs })
            val tlsStats = MetricStatistics.fromValues(timings.mapNotNull { it.tlsMs })
            val reqHeadersStats = MetricStatistics.fromValues(timings.mapNotNull { it.requestHeadersMs })
            val reqBodyStats = MetricStatistics.fromValues(timings.mapNotNull { it.requestBodyMs })
            val respHeadersStats = MetricStatistics.fromValues(timings.mapNotNull { it.responseHeadersMs })
            val respBodyStats = MetricStatistics.fromValues(timings.mapNotNull { it.responseBodyMs })

            return TestCaseResult(
                url,
                totalRequestCountCalc,
                successRequestCountCalc,
                errorRequestCountCalc,
                errorsByType,
                duration = durationStats,
                dns = dnsStats,
                connect = connectStats,
                tls = tlsStats,
                requestHeaders = reqHeadersStats,
                requestBody = reqBodyStats,
                responseHeaders = respHeadersStats,
                responseBody = respBodyStats
            )
        }
    }
}
