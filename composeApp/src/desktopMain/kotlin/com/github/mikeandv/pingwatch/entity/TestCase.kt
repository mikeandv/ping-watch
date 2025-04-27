package com.github.mikeandv.pingwatch.entity

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.StatusCode
import com.github.mikeandv.pingwatch.processor.runR

class TestCase(
    val urls: Map<String, TestCaseParams>,
    val runType: RunType,

) {
    val testCaseState: TestCaseState = TestCaseState()
    lateinit var testCaseResult: List<TestCaseResult>

    suspend fun runTestCase(cancelFlag: () -> Boolean) {
        when (runType) {
            RunType.DURATION -> {
                testCaseState.startDurationTestCase()
                testCaseResult = runR(this, cancelFlag)
                testCaseState.finishDurationTestCase()
            }

            RunType.COUNT -> {
                testCaseState.startCountTestCase()
                testCaseResult = runR(this, cancelFlag)
                testCaseState.finishCountTestCase()
            }
        }
    }

    fun getProgress(): Long {
        return if (testCaseState.getStatus() == StatusCode.CREATED) {
            0
        } else {
            when (runType) {
                RunType.DURATION -> testCaseState.getDurationProgress(urls.values.maxOfOrNull { it.durationValue } ?: 0)

                RunType.COUNT -> testCaseState.getCountProgress(urls.values.sumOf { it.countValue })
            }
        }
    }

    fun getState(): StatusCode {
        return testCaseState.getStatus()
    }

    override fun toString(): String {
        return "TestCase(urls=$urls, runType=$runType, testCaseState=$testCaseState)"
    }
}