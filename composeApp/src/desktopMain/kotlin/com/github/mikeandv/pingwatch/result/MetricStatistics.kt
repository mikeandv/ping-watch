package com.github.mikeandv.pingwatch.result

data class MetricStatistics(
    val min: Long,
    val max: Long,
    val avg: Double,
    val median: Long,
    val p95: Long,
    val p99: Long
) {
    companion object {
        val EMPTY = MetricStatistics(0L, 0L, 0.0, 0L, 0L, 0L)

        fun fromValues(values: List<Long>): MetricStatistics? {
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

        private fun calculatePercentile(sortedList: List<Long>, percentile: Double): Long {
            if (sortedList.isEmpty()) return 0L
            if (percentile !in 0.0..100.0) return 0L
            val index = (percentile / 100.0 * sortedList.size).toInt().coerceAtMost(sortedList.size - 1)
            return sortedList[index]
        }
    }
}
