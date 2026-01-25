package com.github.mikeandv.pingwatch.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestCaseState() {
    private val _status = MutableStateFlow(StatusCode.CREATED)
    val status: StateFlow<StatusCode> get() = _status.asStateFlow()

    fun startCountTestCase() {
        _status.value = StatusCode.RUNNING
    }

    fun finishCountTestCase() {
        _status.value = StatusCode.FINISHED
    }

    fun startDurationTestCase() {
        _status.value = StatusCode.RUNNING
    }

    fun finishDurationTestCase() {
        _status.value = StatusCode.FINISHED
    }

    fun getStatus(): StatusCode {
        return status.value
    }
}