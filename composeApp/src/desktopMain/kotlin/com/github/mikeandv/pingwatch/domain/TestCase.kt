package com.github.mikeandv.pingwatch.domain

import com.github.mikeandv.pingwatch.processor.runR
import com.github.mikeandv.pingwatch.result.TestCaseResult
import kotlinx.coroutines.flow.*

class TestCase(
    val urls: Map<String, TestCaseParams>,
    val runType: RunType,
    val executionMode: ExecutionMode,
    val parallelism: Int,
    val settings: TestCaseSettings,
    val testCaseState: TestCaseState = TestCaseState(),
    testCaseResult: List<TestCaseResult>? = null
) {
    var testCaseResult: List<TestCaseResult> = testCaseResult ?: emptyList()
        private set

    val events = MutableSharedFlow<TestEvent>(
        extraBufferCapacity = 256
    )

    fun totalRequests(): Long =
        urls.values.sumOf { it.countValue }

    fun maxDuration(): Long =
        urls.values.maxOf { it.durationValue }

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

    fun progressFlow(now: () -> Long = { System.currentTimeMillis() }): Flow<Int> {
        return events.runningFold(ProgressState()) { state, event ->
            when (event) {
                is TestEvent.Started ->
                    state.copy(
                        total = event.totalRequests,
                        durationMs = event.durationMs,
                        startedAt = now()
                    )

                is TestEvent.RequestCompleted ->
                    state.copy(completed = state.completed + 1)

                TestEvent.Finished ->
                    state.copy(finished = true)
            }
        }.mapNotNull { state ->
            when {
                state.total != null && state.total > 0 ->
                    ((state.completed * 100) / state.total).toInt()

                state.durationMs != null && state.durationMs > 0 -> {
                    val elapsed = now() - state.startedAt
                    ((elapsed * 100) / state.durationMs).toInt().coerceAtMost(100)
                }

                else -> null
            }
        }.distinctUntilChanged()
    }

    fun urlProgressFlow(url: String): Flow<Int> {
        val total = urls[url]?.countValue ?: 0
        if (total == 0L) return emptyFlow()

        return events.runningFold(0L) { completed, event ->
            when (event) {
                is TestEvent.RequestCompleted ->
                    if (event.url == url) completed + 1 else completed

                else -> completed
            }
        }.map { completed ->
            ((completed * 100) / total).toInt()
        }.distinctUntilChanged()
    }

    fun copy(
        urls: Map<String, TestCaseParams> = this.urls,
        runType: RunType = this.runType,
        executionMode: ExecutionMode = this.executionMode,
        parallelism: Int = this.parallelism,
        settings: TestCaseSettings = this.settings,
        testCaseState: TestCaseState = this.testCaseState,
        testCaseResult: List<TestCaseResult>? = this.testCaseResult
    ): TestCase {
        return TestCase(urls, runType, executionMode, parallelism, settings, testCaseState, testCaseResult)
    }

    override fun toString(): String {
        return "TestCase(urls=$urls, runType=$runType, testCaseState=$testCaseState)"
    }

    data class ProgressState(
        val completed: Long = 0,
        val total: Long? = null,
        val durationMs: Long? = null,
        val startedAt: Long = 0,
        val finished: Boolean = false
    )
}