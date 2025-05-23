package com.github.mikeandv.pingwatch.entity

import com.github.mikeandv.pingwatch.processor.ResponseData

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
    val p99: Long
) {
    companion object {
        fun create(data: List<ResponseData>): List<TestCaseResult> {
            val resultCalc = mutableListOf<TestCaseResult>()
            val parsedData = data.groupBy { it.url }
                .mapValues { (_, results) ->
                    results.groupBy { it.statusCode }
                        .mapValues { (_, reqs) -> reqs.map { it.duration } }
                }
            for ((key, value) in parsedData) {
                resultCalc.add(createInstance(key, value))
            }
            return resultCalc.toList()
        }

        private fun createInstance(url: String, data: Map<Int, List<Long>>): TestCaseResult {
            val totalRequestCountCalc = data.values.sumOf { it.size }
            val successRequestCountCalc = data[200]?.size ?: 0
            val errorRequestCountCalc = data.filterKeys { it != 200 }.values.sumOf { it.size }

            val sorted200 = data[200]?.sorted()

            val minCalc = sorted200?.min() ?: 0
            val maxCalc = sorted200?.max() ?: 0
            val avgCalc = sorted200?.average() ?: 0.0
            val medianCalc = calculatePercentile(sorted200, 50.0)
            val p95Calc = calculatePercentile(sorted200, 95.0)
            val p99Calc = calculatePercentile(sorted200, 99.0)
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
                p99Calc
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