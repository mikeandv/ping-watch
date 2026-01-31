package com.github.mikeandv.pingwatch.result

data class MetricStatistics(
    val min: Double,
    val max: Double,
    val avg: Double,
    val median: Double,
    val p95: Double,
    val p99: Double
) {
    companion object {
        val EMPTY = MetricStatistics(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        fun fromValues(values: List<Double>): MetricStatistics? {
            if (values.isEmpty()) return null

            val sorted = values.sorted()
            return MetricStatistics(
                min = sorted.first(),
                max = sorted.last(),
                avg = sorted.average(),
                median = calculatePercentile(sorted, 50.0),
                p95 = calculatePercentile(sorted, 95.0),
                p99 = calculatePercentile(sorted, 99.0)
            )
        }

        private fun calculatePercentile(sortedList: List<Double>, percentile: Double): Double {
            if (percentile !in 0.0..100.0) return 0.0
            val index = (percentile / 100.0 * sortedList.size).toInt().coerceAtMost(sortedList.size - 1)
            return sortedList[index]
        }
    }
}
