package com.github.mikeandv.pingwatch.result

import java.util.concurrent.CopyOnWriteArrayList

class UrlAvgAggregator {

    private val rawTimings = CopyOnWriteArrayList<RequestTimings>()

    fun add(t: RequestTimings) {
        rawTimings.add(t)
    }

    fun getAllTimings(): List<RequestTimings> = rawTimings.toList()


    fun clear() {
        rawTimings.clear()
    }
}
