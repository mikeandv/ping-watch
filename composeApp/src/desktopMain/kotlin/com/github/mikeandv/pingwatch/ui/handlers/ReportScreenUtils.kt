package com.github.mikeandv.pingwatch.ui.handlers

import com.github.mikeandv.pingwatch.domain.ErrorType
import com.github.mikeandv.pingwatch.domain.TestCase
import com.github.mikeandv.pingwatch.domain.TestCaseParams
import com.github.mikeandv.pingwatch.result.MetricStatistics
import com.github.mikeandv.pingwatch.result.TestCaseResult

// Column weights for consistent table layout
object TableColumnWeights {
    const val EXPAND_BUTTON = 0.4f
    const val URL = 3f
    const val METRIC = 1f
    const val LABEL = 2f
}

fun errorTypeLabel(type: ErrorType): String = when (type) {
    ErrorType.NONE -> "None"
    ErrorType.CONNECTION_REFUSED -> "Connection Refused"
    ErrorType.HOST_UNREACHABLE -> "Host Unreachable"
    ErrorType.TIMEOUT -> "Timeout"
    ErrorType.DNS_FAILURE -> "DNS Failure"
    ErrorType.SSL_ERROR -> "SSL Error"
    ErrorType.NETWORK_ERROR -> "Network Error"
    ErrorType.HTTP_CLIENT_ERROR -> "HTTP Client Error"
    ErrorType.HTTP_CRITICAL_ERROR -> "HTTP Client Error"
    ErrorType.HTTP_SERVER_ERROR -> "HTTP Server Error"
}

fun formatMetricValue(value: Double?): String = value?.let { "%.3f".format(it) } ?: "-"

data class NetworkMetricData(
    val label: String,
    val stats: MetricStatistics?
)

fun getNetworkMetrics(result: TestCaseResult): List<NetworkMetricData> = listOf(
    NetworkMetricData("DNS", result.dns),
    NetworkMetricData("Connect", result.connect),
    NetworkMetricData("TLS", result.tls),
    NetworkMetricData("Req headers", result.requestHeaders),
    NetworkMetricData("Req body", result.requestBody),
    NetworkMetricData("Resp headers", result.responseHeaders),
    NetworkMetricData("Resp body", result.responseBody)
)

fun getTopUrlsByMedian(resultData: List<TestCaseResult>, limit: Int = 5): List<TestCaseResult> =
    resultData.sortedByDescending { it.median }.take(limit)

fun calculateNormalizedMedian(median: Double, maxMedian: Double): Double =
    if (maxMedian > 0) (median / maxMedian) * 100 else 0.0

fun handleCompare(
    testCase: TestCase,
    onUpdateTestCase: (TestCase) -> Unit,
    urlList: Map<String, TestCaseParams>,

    ) {
    val newTestCase = testCase.copy(
        urls = urlList
    )
    onUpdateTestCase(newTestCase)
}
