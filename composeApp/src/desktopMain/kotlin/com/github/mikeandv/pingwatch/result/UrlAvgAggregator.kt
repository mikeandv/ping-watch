package com.github.mikeandv.pingwatch.result

import com.github.mikeandv.pingwatch.domain.EarlyStopTracker
import java.util.concurrent.CopyOnWriteArrayList

class UrlAvgAggregator(earlyStopThreshold: Int) {

    private val rawTimings = CopyOnWriteArrayList<RequestTimings>()
    private val earlyStopTracker = EarlyStopTracker(earlyStopThreshold)

    fun add(t: RequestTimings) {
        rawTimings.add(t)
        earlyStopTracker.record(t.url, t.errorType)
    }

    fun tryAcquireSlot(url: String): Boolean = earlyStopTracker.tryAcquire(url)

    fun shouldStopEarly(url: String): Boolean = earlyStopTracker.shouldStop(url)

    fun getAllTimings(): List<RequestTimings> = rawTimings.toList()

    fun clear() {
        rawTimings.clear()
        earlyStopTracker.clear()
    }

    fun updateEarlyStopThreshold(newThreshold: Int) {
        earlyStopTracker.updateThreshold(newThreshold)
    }
}
