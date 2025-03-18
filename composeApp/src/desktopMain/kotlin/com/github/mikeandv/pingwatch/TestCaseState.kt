package com.github.mikeandv.pingwatch

import java.util.concurrent.atomic.AtomicInteger

class TestCaseState() {
    var status: StatusCode = StatusCode.CREATED
//    var progressInPercent: Double = 0.0
    val executionCounter = AtomicInteger()

    fun getDurationProgress(count: Int): Int {
        return (100 * executionCounter.get()) / count
    }
}
