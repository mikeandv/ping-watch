package com.github.mikeandv.pingwatch.entity

import com.github.mikeandv.pingwatch.StatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

class TestCaseState() {
    private val _status = MutableStateFlow(StatusCode.CREATED)
    val status: StateFlow<StatusCode> get() = _status.asStateFlow()

    private val executionCounter = AtomicLong()
    private val startTime = AtomicLong()

    fun getCountProgress(count: Long): Long {
        return ((100 * executionCounter.get()) / count)
    }

    fun getDurationProgress(durationMillis: Long): Long {
        val elapsedTime = System.currentTimeMillis() - startTime.get()
        return (100 * (elapsedTime.toFloat() / durationMillis).coerceIn(0f, 1f)).toLong()
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
        return status.value
    }

    fun getExecutionCounter(): AtomicLong {
        return executionCounter
    }
}