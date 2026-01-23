package com.github.mikeandv.pingwatch.entity

class TestCaseResult private constructor(
    val url: String,
    val totalRequestCount: Int,
    val successRequestCount: Int,
    val errorRequestCount: Int,
    val min: Long,
    val max: Long,
    val avg: Double,
    val median: Long,
    val p95: Long,
    val p99: Long,

    val dnsMs: Long?,
    val connectMs: Long?,
    val tlsMs: Long?,
    val requestHeadersMs: Long?,
    val requestBodyMs: Long?,
    val responseHeadersMs: Long?,
    val responseBodyMs: Long?
) {
    companion object {
        fun create(data: List<ResponseData>): List<TestCaseResult> =
            create(data, emptyMap())

        fun create(data: List<ResponseData>, metricsByUrl: Map<String, UrlAvgMetrics>): List<TestCaseResult> {
            val resultCalc = mutableListOf<TestCaseResult>()

            val parsedData = data.groupBy { it.url }
                .mapValues { (_, results) ->
                    results.groupBy { it.statusCode }
                        .mapValues { (_, reqs) -> reqs.map { it.duration } }
                }

            for ((key, value) in parsedData) {
                resultCalc.add(createInstance(key, value, metricsByUrl[key]))
            }

            return resultCalc.toList()
        }

        private fun createInstance(url: String, data: Map<Int, List<Long>>, metrics: UrlAvgMetrics?): TestCaseResult {
            val totalRequestCountCalc = data.values.sumOf { it.size }
            val successDurations = data
                .filterKeys { it in 200..399 }
                .values
                .flatten()
                .sorted()

            val successRequestCountCalc = successDurations.size
            val errorRequestCountCalc = totalRequestCountCalc - successRequestCountCalc

            val minCalc = successDurations.minOrNull() ?: 0L
            val maxCalc = successDurations.maxOrNull() ?: 0L
            val avgCalc = if (successDurations.isEmpty()) 0.0 else successDurations.average()
            val medianCalc = calculatePercentile(successDurations, 50.0)
            val p95Calc = calculatePercentile(successDurations, 95.0)
            val p99Calc = calculatePercentile(successDurations, 99.0)
            return TestCaseResult(
                url,
                totalRequestCountCalc,
                successRequestCountCalc,
                errorRequestCountCalc,
                minCalc,
                maxCalc,
                avgCalc,
                medianCalc,
                p95Calc,
                p99Calc,

                dnsMs = metrics?.dnsAvgMs?.toLong(),
                connectMs = metrics?.connectAvgMs?.toLong(),
                tlsMs = metrics?.tlsAvgMs?.toLong(),
                requestHeadersMs = metrics?.requestHeadersAvgMs?.toLong(),
                requestBodyMs = metrics?.requestBodyAvgMs?.toLong(),
                responseHeadersMs = metrics?.responseHeadersAvgMs?.toLong(),
                responseBodyMs = metrics?.responseBodyAvgMs?.toLong()
            )
        }

        private fun calculatePercentile(sortedList: List<Long>?, percentile: Double): Long {
            if (sortedList.isNullOrEmpty()) {
                println("Sorted list cannot be null or empty!")
                return 0L
            }
            if (percentile !in 0.0..100.0) {
                println("Percentile must be number between 0 and 100")
                return 0L
            }
            val index = (percentile / 100.0 * sortedList.size).toInt().coerceAtMost(sortedList.size - 1)
            return sortedList[index]
        }
    }

    override fun toString(): String {
        return "TestCaseResult(url='$url', totalRequestCount=$totalRequestCount, successRequestCount=$successRequestCount, errorRequestCount=$errorRequestCount, min=$min, max=$max, avg=$avg, median=$median, p95=$p95, p99=$p99)"
    }
}