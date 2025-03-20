package com.github.mikeandv.pingwatch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class TestCaseState() {
    private val _status = MutableStateFlow(StatusCode.CREATED)
    val status: StateFlow<StatusCode> get() = _status.asStateFlow()

//    private var status: StatusCode = StatusCode.CREATED

    //    var progressInPercent: Double = 0.0
    private val executionCounter = AtomicInteger()
    private val startTime = AtomicLong()

    fun getCountProgress(count: Int): Int {
        return (100 * executionCounter.get()) / count
    }

    fun getDurationProgress(durationMillis: Long): Int {
        val elapsedTime = System.currentTimeMillis() - startTime.get()
        return (100 * (elapsedTime.toFloat() / durationMillis).coerceIn(0f, 1f)).toInt()
    }

    fun startCountTestCase() {
        _status.value = StatusCode.RUNNING
    }

    fun finishCountTestCase() {
        _status.value = StatusCode.FINISHED
    }

    fun startDurationTestCase() {
        startTime.set(System.currentTimeMillis())
        _status.value = StatusCode.RUNNING
    }

    fun finishDurationTestCase() {
        _status.value = StatusCode.FINISHED
    }

    fun getStatus(): StatusCode {
        return _status.value
    }

    fun getExecutionCounter(): AtomicInteger {
        return executionCounter
    }
}