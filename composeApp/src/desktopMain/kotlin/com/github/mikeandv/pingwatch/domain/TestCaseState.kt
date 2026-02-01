package com.github.mikeandv.pingwatch.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestCaseState() {
    private val _status = MutableStateFlow(StatusCode.CREATED)
    val status: StateFlow<StatusCode> get() = _status.asStateFlow()

    private val _runId = MutableStateFlow(0L)
    val runId: StateFlow<Long> get() = _runId.asStateFlow()

    fun startTestCase() {
        _runId.value++
        _status.value = StatusCode.RUNNING
    }

    fun finishTestCase() {
        _status.value = StatusCode.FINISHED
    }

    fun getStatus(): StatusCode {
        return status.value
    }
}