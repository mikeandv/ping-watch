package com.github.mikeandv.pingwatch.entity

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.StatusCode
import com.github.mikeandv.pingwatch.processor.runR

class TestCase(
    val urls: List<String>,
    val runType: RunType,
    val countValue: Long?,
    val durationValue: Long?,
) {
    val testCaseState: TestCaseState = TestCaseState()
    lateinit var testCaseResult: List<TestCaseResult>

    suspend fun runTestCase() {
        when (runType) {
            RunType.DURATION -> {
                testCaseState.startDurationTestCase()
                testCaseResult = runR(this)
                testCaseState.finishDurationTestCase()
            }

            RunType.COUNT -> {
                testCaseState.startCountTestCase()
                testCaseResult = runR(this)
                testCaseState.finishCountTestCase()
            }
        }
    }

    fun getProgress(): Long {
        return if (testCaseState.getStatus() == StatusCode.CREATED) {
            0
        } else {
            when (runType) {
                RunType.DURATION -> testCaseState.getDurationProgress(durationValue ?: 0)
                RunType.COUNT -> testCaseState.getCountProgress((countValue ?: 0) * urls.size)
            }
        }
    }

    fun getState(): StatusCode {
        return testCaseState.getStatus()
    }

    override fun toString(): String {
        return "TestCase(urls=$urls, runType=$runType, countValue=$countValue, durationValue=$durationValue, testCaseState=$testCaseState)"
    }


}