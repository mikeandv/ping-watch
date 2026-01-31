package com.github.mikeandv.pingwatch.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestCaseState() {
    private val _status = MutableStateFlow(StatusCode.CREATED)
    val status: StateFlow<StatusCode> get() = _status.asStateFlow()

    fun startTestCase() {
        _status.value = StatusCode.RUNNING
    }

    fun finishTestCase() {
        _status.value = StatusCode.FINISHED
    }

    fun getStatus(): StatusCode {
        return status.value
    }
}