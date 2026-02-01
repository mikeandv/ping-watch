package com.github.mikeandv.pingwatch.domain

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ProgressFlowTests {

    private fun createTestCaseParams(
        countValue: Long = 0L,
        durationValue: Long = 0L,
        unformattedDurationValue: String = "",
        tag: Category? = null
    ): TestCaseParams {
        return TestCaseParams(
            isEdit = false,
            countValue = countValue,
            durationValue = durationValue,
            unformattedDurationValue = unformattedDurationValue,
            tag = tag
        )
    }

    private fun createTestCase(
        urls: Map<String, TestCaseParams> = mapOf(
            "https://example.com/" to createTestCaseParams(countValue = 10)
        ),
        runType: RunType = RunType.COUNT,
        executionMode: ExecutionMode = ExecutionMode.SEQUENTIAL,
        parallelism: Int = 1
    ): TestCase {
        return TestCase(
            urls = urls,
            runType = runType,
            executionMode = executionMode,
            parallelism = parallelism,
            settings = TestCaseSettings()
        )
    }

    // progressFlow tests for COUNT mode

    @Test
    fun `progressFlow should emit 0 initially for COUNT mode`() = runTest {
        val testCase = createTestCase()

        testCase.progressFlow().test {
            testCase.events.emit(TestEvent.Started(totalRequests = 10, null))
            assertEquals(0, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `progressFlow should calculate percentage based on completed requests`() = runTest {
        val testCase = createTestCase()

        testCase.progressFlow().test {
            testCase.events.emit(TestEvent.Started(totalRequests = 10, null))
            assertEquals(0, awaitItem())

            testCase.events.emit(TestEvent.RequestCompleted("https://example.com/"))
            assertEquals(10, awaitItem())

            testCase.events.emit(TestEvent.RequestCompleted("https://example.com/"))
            assertEquals(20, awaitItem())

            testCase.events.emit(TestEvent.RequestCompleted("https://example.com/"))
            assertEquals(30, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `progressFlow should emit 100 on Finished event`() = runTest {
        val testCase = createTestCase()

        testCase.progressFlow().test {
            testCase.events.emit(TestEvent.Started(totalRequests = 10, null))
            assertEquals(0, awaitItem())

            testCase.events.emit(TestEvent.Finished)
            assertEquals(100, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `progressFlow should emit 100 when all requests completed`() = runTest {
        val testCase = createTestCase(
            urls = mapOf("https://example.com/" to createTestCaseParams(countValue = 2))
        )

        testCase.progressFlow().test {
            testCase.events.emit(TestEvent.Started(totalRequests = 2, null))
            assertEquals(0, awaitItem())

            testCase.events.emit(TestEvent.RequestCompleted("https://example.com/"))
            assertEquals(50, awaitItem())

            testCase.events.emit(TestEvent.RequestCompleted("https://example.com/"))
            assertEquals(100, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    // progressFlow tests for DURATION mode

    @Test
    fun `progressFlow should calculate percentage based on elapsed time for DURATION mode`() = runTest {
        val testCase = createTestCase(runType = RunType.DURATION)
        var currentTime = 0L

        testCase.progressFlow(now = { currentTime }).test {
            testCase.events.emit(TestEvent.Started(totalRequests = null, durationMs = 1000))
            assertEquals(0, awaitItem())

            currentTime = 500
            testCase.events.emit(TestEvent.RequestCompleted("https://example.com/"))
            assertEquals(50, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    // urlRequestCountFlow tests

    @Test
    fun `urlRequestCountFlow should start at 0`() = runTest {
        val testCase = createTestCase()

        testCase.urlProgressFlow("https://example.com/").test {
            assertEquals(0L, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `urlRequestCountFlow should count requests for specific URL`() = runTest {
        val testCase = createTestCase()

        testCase.urlProgressFlow("https://example.com/").test {
            assertEquals(0L, awaitItem())

            testCase.events.emit(TestEvent.RequestCompleted("https://example.com/"))
            assertEquals(1L, awaitItem())

            testCase.events.emit(TestEvent.RequestCompleted("https://example.com/"))
            assertEquals(2L, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `urlRequestCountFlow should ignore requests for other URLs`() = runTest {
        val testCase = createTestCase(
            urls = mapOf(
                "https://example.com/" to createTestCaseParams(countValue = 10),
                "https://other.com/" to createTestCaseParams(countValue = 10)
            )
        )

        testCase.urlProgressFlow("https://example.com/").test {
            assertEquals(0L, awaitItem())

            testCase.events.emit(TestEvent.RequestCompleted("https://other.com/"))
            testCase.events.emit(TestEvent.RequestCompleted("https://other.com/"))
            // No new emissions - other URL requests are ignored

            testCase.events.emit(TestEvent.RequestCompleted("https://example.com/"))
            assertEquals(1L, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `urlRequestCountFlow should handle Started and Finished events without counting`() = runTest {
        val testCase = createTestCase()

        testCase.urlProgressFlow("https://example.com/").test {
            assertEquals(0L, awaitItem())

            testCase.events.emit(TestEvent.Started(totalRequests = 10, durationMs = null))
            // Started event doesn't change count

            testCase.events.emit(TestEvent.RequestCompleted("https://example.com/"))
            assertEquals(1L, awaitItem())

            testCase.events.emit(TestEvent.Finished)
            // Finished event doesn't change count

            cancelAndConsumeRemainingEvents()
        }
    }
}
