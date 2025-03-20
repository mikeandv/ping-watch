package com.github.mikeandv.pingwatch

class TestCase(
    val urls: List<String>,
    val runType: RunType,
    val countValue: Int,
    val durationValue: Long?,
) {
    val testCaseState: TestCaseState = TestCaseState()
    lateinit var testCaseResult: List<TestCaseResult>

    suspend fun runTestCase() {
        when(runType) {
            RunType.DURATION -> {
                testCaseState.startDurationTestCase()
//                println(this)
                testCaseResult = runR(this)
                testCaseState.finishDurationTestCase()
            }
            RunType.COUNT -> {
                testCaseState.startCountTestCase()
//                println(this)
                testCaseResult = runR(this)
                testCaseState.finishCountTestCase()
            }
        }

//        return testCaseResult
    }

    fun getProgress(): Int {
        return if (testCaseState.getStatus() == StatusCode.CREATED) {
            0
        } else {
            when(runType) {
                RunType.DURATION -> testCaseState.getDurationProgress(durationValue?: 0)
                RunType.COUNT -> testCaseState.getCountProgress(countValue * urls.size)
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