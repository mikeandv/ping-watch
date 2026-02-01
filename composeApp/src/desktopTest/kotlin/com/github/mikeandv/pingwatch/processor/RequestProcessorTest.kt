package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.domain.*
import com.github.mikeandv.pingwatch.result.RequestTimings
import com.github.mikeandv.pingwatch.result.UrlAvgAggregator
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.*

class RequestProcessorTest {

    private lateinit var mockWebServer: MockWebServer

    @BeforeTest
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @AfterTest
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun createTestCaseParams(count: Long = 1, duration: Long = 1000): TestCaseParams {
        return TestCaseParams(
            isEdit = false,
            countValue = count,
            durationValue = duration,
            unformattedDurationValue = "${duration}ms",
            tag = null
        )
    }

    private fun createTestCase(
        urls: Map<String, TestCaseParams>,
        runType: RunType,
        executionMode: ExecutionMode,
        parallelism: Int = 4,
        earlyStopThreshold: Int = 5
    ): TestCase {
        val settings = TestCaseSettings(earlyStopThreshold = earlyStopThreshold)
        val agg = UrlAvgAggregator(earlyStopThreshold)
        return TestCase(
            urls = urls,
            runType = runType,
            executionMode = executionMode,
            parallelism = parallelism,
            settings = settings,
            agg = agg
        )
    }

    // RunType.COUNT + ExecutionMode.SEQUENTIAL

    @Test
    fun `runR with COUNT SEQUENTIAL should make correct number of requests`() = runTest {
        val requestCount = 3
        repeat(requestCount) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val url = mockWebServer.url("/test").toString()
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(count = requestCount.toLong())),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        runR(testCase, cancelFlag = { false })

        assertEquals(requestCount, mockWebServer.requestCount)
    }

    @Test
    fun `runR with COUNT SEQUENTIAL should return results with correct metrics`() = runTest {
        val requestCount = 5
        repeat(requestCount) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val url = mockWebServer.url("/test").toString()
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(count = requestCount.toLong())),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        val results = runR(testCase, cancelFlag = { false })

        assertEquals(1, results.size)
        assertEquals(url, results[0].url)
        assertEquals(requestCount, results[0].totalRequestCount)
        assertEquals(requestCount, results[0].successRequestCount)
        assertEquals(0, results[0].errorRequestCount)
    }

    @Test
    fun `runR with COUNT SEQUENTIAL should handle multiple URLs`() = runTest {
        val requestCountPerUrl = 2
        val url1 = mockWebServer.url("/test1").toString()
        val url2 = mockWebServer.url("/test2").toString()

        repeat(requestCountPerUrl * 2) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val testCase = createTestCase(
            urls = mapOf(
                url1 to createTestCaseParams(count = requestCountPerUrl.toLong()),
                url2 to createTestCaseParams(count = requestCountPerUrl.toLong())
            ),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        val results = runR(testCase, cancelFlag = { false })

        assertEquals(2, results.size)
        assertEquals(requestCountPerUrl * 2, mockWebServer.requestCount)
    }

    @Test
    fun `runR with COUNT SEQUENTIAL should stop when cancel flag is true`() = runTest {
        val requestCount = 10
        repeat(requestCount) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val url = mockWebServer.url("/test").toString()
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(count = requestCount.toLong())),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        var requestsMade = 0
        runR(testCase, cancelFlag = {
            requestsMade++
            requestsMade > 3
        })

        assertTrue(mockWebServer.requestCount < requestCount)
    }

    // RunType.COUNT + ExecutionMode.PARALLEL

    @Test
    fun `runR with COUNT PARALLEL should make correct number of requests`() = runTest {
        val requestCount = 5
        repeat(requestCount) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val url = mockWebServer.url("/test").toString()
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(count = requestCount.toLong())),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.PARALLEL,
            parallelism = 2
        )

        runR(testCase, cancelFlag = { false })

        assertEquals(requestCount, mockWebServer.requestCount)
    }

    @Test
    fun `runR with COUNT PARALLEL should return results for all URLs`() = runTest {
        val requestCount = 2
        val url1 = mockWebServer.url("/test1").toString()
        val url2 = mockWebServer.url("/test2").toString()

        repeat(requestCount * 2) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val testCase = createTestCase(
            urls = mapOf(
                url1 to createTestCaseParams(count = requestCount.toLong()),
                url2 to createTestCaseParams(count = requestCount.toLong())
            ),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.PARALLEL,
            parallelism = 4
        )

        val results = runR(testCase, cancelFlag = { false })

        assertEquals(2, results.size)
        val urls = results.map { it.url }.toSet()
        assertTrue(urls.contains(url1))
        assertTrue(urls.contains(url2))
    }

    @Test
    fun `runR with COUNT PARALLEL should stop when cancel flag is true`() = runTest {
        val requestCount = 20
        repeat(requestCount) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val url = mockWebServer.url("/test").toString()
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(count = requestCount.toLong())),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.PARALLEL,
            parallelism = 2
        )

        var cancelled = false
        runR(testCase, cancelFlag = {
            if (mockWebServer.requestCount >= 5) {
                cancelled = true
            }
            cancelled
        })

        assertTrue(mockWebServer.requestCount < requestCount)
    }

    // RunType.DURATION + ExecutionMode.SEQUENTIAL

    @Test
    fun `runR with DURATION SEQUENTIAL should run for specified duration`() = runTest {
        repeat(100) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val url = mockWebServer.url("/test").toString()
        val durationMs = 200L
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(duration = durationMs)),
            runType = RunType.DURATION,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        val startTime = System.currentTimeMillis()
        runR(testCase, cancelFlag = { false })
        val elapsed = System.currentTimeMillis() - startTime

        assertTrue(elapsed >= durationMs - 50, "Should run for at least the duration minus network overhead")
    }

    @Test
    fun `runR with DURATION SEQUENTIAL should stop when cancel flag is true`() = runTest {
        repeat(100) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val url = mockWebServer.url("/test").toString()
        val fullDuration = 5000L
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(duration = fullDuration)),
            runType = RunType.DURATION,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        var requestCount = 0
        val startTime = System.currentTimeMillis()
        runR(testCase, cancelFlag = {
            requestCount++
            requestCount > 3
        })
        val elapsed = System.currentTimeMillis() - startTime

        assertTrue(elapsed < fullDuration, "Should stop before full duration when cancelled")
    }

    // RunType.DURATION + ExecutionMode.PARALLEL

    @Test
    fun `runR with DURATION PARALLEL should run for specified duration`() = runTest {
        repeat(100) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val url = mockWebServer.url("/test").toString()
        val durationMs = 200L
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(duration = durationMs)),
            runType = RunType.DURATION,
            executionMode = ExecutionMode.PARALLEL,
            parallelism = 2
        )

        val startTime = System.currentTimeMillis()
        runR(testCase, cancelFlag = { false })
        val elapsed = System.currentTimeMillis() - startTime

        assertTrue(elapsed >= durationMs - 50, "Should run for at least the duration minus network overhead")
    }

    @Test
    fun `runR with DURATION PARALLEL should stop when cancel flag is true`() = runTest {
        repeat(100) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val url = mockWebServer.url("/test").toString()
        val fullDuration = 10000L
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(duration = fullDuration)),
            runType = RunType.DURATION,
            executionMode = ExecutionMode.PARALLEL,
            parallelism = 2
        )

        var checkCount = 0
        val startTime = System.currentTimeMillis()
        runR(testCase, cancelFlag = {
            checkCount++
            checkCount > 5
        })
        val elapsed = System.currentTimeMillis() - startTime

        assertTrue(elapsed < fullDuration, "Should stop before full duration when cancelled")
    }

    // Error handling tests

    @Test
    fun `runR should count errors correctly for failed requests`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        val url = mockWebServer.url("/test").toString()
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(count = 3)),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        val results = runR(testCase, cancelFlag = { false })

        assertEquals(1, results.size)
        assertEquals(3, results[0].totalRequestCount)
        assertEquals(1, results[0].successRequestCount)
        assertEquals(2, results[0].errorRequestCount)
    }

    @Test
    fun `runR should record status codes correctly`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(201))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val url = mockWebServer.url("/test").toString()
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(count = 3)),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        val results = runR(testCase, cancelFlag = { false })

        assertEquals(2, results[0].statusCodeCounts[200])
        assertEquals(1, results[0].statusCodeCounts[201])
    }

    // Aggregator interaction tests

    @Test
    fun `runR should clear aggregator at start`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val url = mockWebServer.url("/test").toString()
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(count = 1)),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        testCase.agg.add(
            RequestTimings(
                url = "http://old.url",
                success = true,
                statusCode = 200,
                error = null,
                callMs = 100.0,
                dnsMs = null,
                connectMs = null,
                tlsMs = null,
                requestHeadersMs = null,
                requestBodyMs = null,
                responseHeadersMs = null,
                responseBodyMs = null
            )
        )

        runR(testCase, cancelFlag = { false })

        val timings = testCase.agg.getAllTimings()
        assertTrue(timings.all { it.url == url })
    }

    // Result creation tests

    @Test
    fun `runR should return empty list when no requests made`() = runTest {
        val url = mockWebServer.url("/test").toString()
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(count = 0)),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        val results = runR(testCase, cancelFlag = { false })

        assertTrue(results.isEmpty())
    }

    @Test
    fun `runR should calculate metrics correctly for successful requests`() = runTest {
        repeat(5) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val url = mockWebServer.url("/test").toString()
        val testCase = createTestCase(
            urls = mapOf(url to createTestCaseParams(count = 5)),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        val results = runR(testCase, cancelFlag = { false })

        assertEquals(1, results.size)
        assertTrue(results[0].min > 0)
        assertTrue(results[0].max >= results[0].min)
        assertTrue(results[0].avg >= results[0].min)
        assertTrue(results[0].avg <= results[0].max)
    }

    // totalRequests and maxDuration tests

    @Test
    fun `totalRequests should sum count values from all URLs`() = runTest {
        val url1 = mockWebServer.url("/test1").toString()
        val url2 = mockWebServer.url("/test2").toString()

        val testCase = createTestCase(
            urls = mapOf(
                url1 to createTestCaseParams(count = 5),
                url2 to createTestCaseParams(count = 3)
            ),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        assertEquals(8, testCase.totalRequests())
    }

    @Test
    fun `maxDuration should return maximum duration from all URLs`() = runTest {
        val url1 = mockWebServer.url("/test1").toString()
        val url2 = mockWebServer.url("/test2").toString()

        val testCase = createTestCase(
            urls = mapOf(
                url1 to createTestCaseParams(duration = 1000),
                url2 to createTestCaseParams(duration = 2000)
            ),
            runType = RunType.DURATION,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        assertEquals(2000, testCase.maxDuration())
    }
}
