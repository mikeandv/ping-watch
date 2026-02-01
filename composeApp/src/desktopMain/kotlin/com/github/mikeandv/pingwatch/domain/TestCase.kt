package com.github.mikeandv.pingwatch.domain

import com.github.mikeandv.pingwatch.processor.runR
import com.github.mikeandv.pingwatch.result.TestCaseResult
import com.github.mikeandv.pingwatch.result.UrlAvgAggregator
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient

class TestCase(
    val urls: Map<String, TestCaseParams>,
    val runType: RunType,
    val executionMode: ExecutionMode,
    val parallelism: Int,
    val testCaseState: TestCaseState = TestCaseState(),
    settings: TestCaseSettings,
    testCaseResult: List<TestCaseResult>? = null,
    agg: UrlAvgAggregator? = null,
    okHttpClient: OkHttpClient? = null
) {
    val agg: UrlAvgAggregator = agg ?: UrlAvgAggregator(settings.earlyStopThreshold)
    val events = MutableSharedFlow<TestEvent>(
        extraBufferCapacity = 256
    )
    var testCaseResult: List<TestCaseResult> = testCaseResult ?: emptyList()
        private set

    var settings: TestCaseSettings = settings
        private set

    var okHttpClient: OkHttpClient = okHttpClient ?: settings.createHttpClient(this.agg)
        private set

    fun updateSettings(newSettings: TestCaseSettings) {
        if (newSettings.earlyStopThreshold != settings.earlyStopThreshold) {
            agg.updateEarlyStopThreshold(newSettings.earlyStopThreshold)
        }
        okHttpClient = newSettings.createHttpClient(agg)
        settings = newSettings
    }

    fun totalRequests(): Long =
        urls.values.sumOf { it.countValue }

    fun maxDuration(): Long =
        urls.values.maxOf { it.durationValue }

    suspend fun runTestCase(cancelFlag: () -> Boolean) {
        when (runType) {
            RunType.DURATION -> {
                testCaseState.startTestCase()
                testCaseResult = runR(this, cancelFlag)
                testCaseState.finishTestCase()
            }

            RunType.COUNT -> {
                testCaseState.startTestCase()
                testCaseResult = runR(this, cancelFlag)
                testCaseState.finishTestCase()
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
                state.finished -> 100

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

    fun urlRequestCountFlow(url: String): Flow<Long> {
        return events.runningFold(0L) { count, event ->
            when (event) {
                is TestEvent.RequestCompleted ->
                    if (event.url == url) count + 1 else count

                else -> count
            }
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
        return TestCase(
            urls, runType, executionMode, parallelism, testCaseState, settings, testCaseResult,
            agg = this.agg,
            okHttpClient = this.okHttpClient
        )
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