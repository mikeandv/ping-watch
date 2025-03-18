package com.github.mikeandv.pingwatch

class TestCase(
    val urls: List<String>,
    val runType: RunType,
    val countValue: Int,
    val durationValue: Int,
) {
    val testCaseState: TestCaseState = TestCaseState()
    lateinit var testCaseResult: TestCaseResult

    suspend fun runTestCase(): String {
        this.testCaseState.status = StatusCode.RUNNING
        testCaseResult = runR(this)
        this.testCaseState.status = StatusCode.FINISHED
        return testCaseResult.toString()
    }

    fun getProgress(): Int {
        return when(runType) {
            RunType.DURATION -> 0
            RunType.COUNT -> testCaseState.getDurationProgress(countValue * urls.size)
        }
    }
    fun getState(): StatusCode {
        return testCaseState.status
    }
}