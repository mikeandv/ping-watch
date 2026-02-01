package com.github.mikeandv.pingwatch.result

import com.github.mikeandv.pingwatch.domain.Category
import com.github.mikeandv.pingwatch.domain.ErrorType
import com.github.mikeandv.pingwatch.domain.TestCaseParams
import kotlin.test.*

class TestCaseResultTest {

    private fun createTiming(
        url: String,
        success: Boolean = true,
        statusCode: Int? = 200,
        errorType: ErrorType = ErrorType.NONE,
        callMs: Double = 100.0,
        dnsMs: Double? = null,
        connectMs: Double? = null,
        tlsMs: Double? = null
    ) = RequestTimings(
        url = url,
        success = success,
        statusCode = statusCode,
        error = null,
        errorType = errorType,
        callMs = callMs,
        dnsMs = dnsMs,
        connectMs = connectMs,
        tlsMs = tlsMs,
        requestHeadersMs = null,
        requestBodyMs = null,
        responseHeadersMs = null,
        responseBodyMs = null
    )

    // create tests

    @Test
    fun `create should return empty list for empty timings`() {
        val results = TestCaseResult.create(emptyList())
        assertTrue(results.isEmpty())
    }

    @Test
    fun `create should group timings by URL`() {
        val timings = listOf(
            createTiming("https://example1.com"),
            createTiming("https://example2.com"),
            createTiming("https://example1.com")
        )

        val results = TestCaseResult.create(timings)

        assertEquals(2, results.size)
    }

    @Test
    fun `create should calculate totalRequestCount correctly`() {
        val timings = listOf(
            createTiming("https://example.com"),
            createTiming("https://example.com"),
            createTiming("https://example.com")
        )

        val results = TestCaseResult.create(timings)

        assertEquals(1, results.size)
        assertEquals(3, results[0].totalRequestCount)
    }

    @Test
    fun `create should calculate successRequestCount correctly`() {
        val timings = listOf(
            createTiming("https://example.com", success = true, statusCode = 200),
            createTiming("https://example.com", success = true, statusCode = 201),
            createTiming("https://example.com", success = false, statusCode = 500, errorType = ErrorType.HTTP_SERVER_ERROR)
        )

        val results = TestCaseResult.create(timings)

        assertEquals(2, results[0].successRequestCount)
        assertEquals(1, results[0].errorRequestCount)
    }

    @Test
    fun `create should calculate errorRequestCount correctly`() {
        val timings = listOf(
            createTiming("https://example.com", success = true, statusCode = 200),
            createTiming("https://example.com", success = false, statusCode = 404, errorType = ErrorType.HTTP_CRITICAL_ERROR),
            createTiming("https://example.com", success = false, statusCode = 500, errorType = ErrorType.HTTP_SERVER_ERROR)
        )

        val results = TestCaseResult.create(timings)

        assertEquals(1, results[0].successRequestCount)
        assertEquals(2, results[0].errorRequestCount)
    }

    @Test
    fun `create should group network errors by type`() {
        val timings = listOf(
            createTiming("https://example.com", success = false, statusCode = null, errorType = ErrorType.DNS_FAILURE),
            createTiming("https://example.com", success = false, statusCode = null, errorType = ErrorType.DNS_FAILURE),
            createTiming("https://example.com", success = false, statusCode = null, errorType = ErrorType.TIMEOUT)
        )

        val results = TestCaseResult.create(timings)

        assertEquals(2, results[0].networkErrorsByType[ErrorType.DNS_FAILURE])
        assertEquals(1, results[0].networkErrorsByType[ErrorType.TIMEOUT])
    }

    @Test
    fun `create should group HTTP errors by type`() {
        val timings = listOf(
            createTiming("https://example.com", success = false, statusCode = 404, errorType = ErrorType.HTTP_CRITICAL_ERROR),
            createTiming("https://example.com", success = false, statusCode = 500, errorType = ErrorType.HTTP_SERVER_ERROR),
            createTiming("https://example.com", success = false, statusCode = 503, errorType = ErrorType.HTTP_SERVER_ERROR)
        )

        val results = TestCaseResult.create(timings)

        assertEquals(1, results[0].httpErrorsByType[ErrorType.HTTP_CRITICAL_ERROR])
        assertEquals(2, results[0].httpErrorsByType[ErrorType.HTTP_SERVER_ERROR])
    }

    @Test
    fun `create should count status codes correctly`() {
        val timings = listOf(
            createTiming("https://example.com", statusCode = 200),
            createTiming("https://example.com", statusCode = 200),
            createTiming("https://example.com", statusCode = 201),
            createTiming("https://example.com", statusCode = 404)
        )

        val results = TestCaseResult.create(timings)

        assertEquals(2, results[0].statusCodeCounts[200])
        assertEquals(1, results[0].statusCodeCounts[201])
        assertEquals(1, results[0].statusCodeCounts[404])
    }

    @Test
    fun `create should calculate duration statistics from successful requests only`() {
        val timings = listOf(
            createTiming("https://example.com", success = true, statusCode = 200, callMs = 100.0),
            createTiming("https://example.com", success = true, statusCode = 200, callMs = 200.0),
            createTiming("https://example.com", success = false, statusCode = 500, errorType = ErrorType.HTTP_SERVER_ERROR, callMs = 1000.0)
        )

        val results = TestCaseResult.create(timings)

        assertEquals(100.0, results[0].duration.min)
        assertEquals(200.0, results[0].duration.max)
        assertEquals(150.0, results[0].duration.avg)
    }

    @Test
    fun `create should calculate DNS statistics`() {
        val timings = listOf(
            createTiming("https://example.com", dnsMs = 10.0),
            createTiming("https://example.com", dnsMs = 20.0),
            createTiming("https://example.com", dnsMs = 30.0)
        )

        val results = TestCaseResult.create(timings)

        assertNotNull(results[0].dns)
        assertEquals(10.0, results[0].dns?.min)
        assertEquals(30.0, results[0].dns?.max)
    }

    @Test
    fun `create should return null DNS statistics when no DNS data`() {
        val timings = listOf(
            createTiming("https://example.com", dnsMs = null),
            createTiming("https://example.com", dnsMs = null)
        )

        val results = TestCaseResult.create(timings)

        assertNull(results[0].dns)
    }

    @Test
    fun `create should calculate connect and TLS statistics`() {
        val timings = listOf(
            createTiming("https://example.com", connectMs = 50.0, tlsMs = 100.0),
            createTiming("https://example.com", connectMs = 60.0, tlsMs = 120.0)
        )

        val results = TestCaseResult.create(timings)

        assertNotNull(results[0].connect)
        assertNotNull(results[0].tls)
        assertEquals(50.0, results[0].connect?.min)
        assertEquals(100.0, results[0].tls?.min)
    }

    // Convenience accessors tests

    @Test
    fun `convenience accessors should delegate to duration`() {
        val timings = listOf(
            createTiming("https://example.com", callMs = 100.0),
            createTiming("https://example.com", callMs = 200.0)
        )

        val results = TestCaseResult.create(timings)
        val result = results[0]

        assertEquals(result.duration.min, result.min)
        assertEquals(result.duration.max, result.max)
        assertEquals(result.duration.avg, result.avg)
        assertEquals(result.duration.median, result.median)
        assertEquals(result.duration.p95, result.p95)
        assertEquals(result.duration.p99, result.p99)
    }

    // createForTag tests

    @Test
    fun `createForTag should aggregate timings for URLs with matching tag`() {
        val tag = Category(id = 1, name = "API")
        val params1 = TestCaseParams(isEdit = false, countValue = 10, durationValue = 0, unformattedDurationValue = "", tag = tag)
        val params2 = TestCaseParams(isEdit = false, countValue = 10, durationValue = 0, unformattedDurationValue = "", tag = null)

        val urlsWithParams = mapOf(
            "https://api.example.com" to params1,
            "https://web.example.com" to params2
        )

        val timings = listOf(
            createTiming("https://api.example.com", callMs = 100.0),
            createTiming("https://api.example.com", callMs = 200.0),
            createTiming("https://web.example.com", callMs = 300.0)
        )

        val result = TestCaseResult.createForTag(tag, urlsWithParams, timings)

        assertEquals("API", result.url)
        assertEquals(2, result.totalRequestCount)
    }

    @Test
    fun `createForTag should return empty result when no URLs match tag`() {
        val tag = Category(id = 1, name = "API")
        val params = TestCaseParams(isEdit = false, countValue = 10, durationValue = 0, unformattedDurationValue = "", tag = null)

        val urlsWithParams = mapOf(
            "https://example.com" to params
        )

        val timings = listOf(
            createTiming("https://example.com", callMs = 100.0)
        )

        val result = TestCaseResult.createForTag(tag, urlsWithParams, timings)

        assertEquals("API", result.url)
        assertEquals(0, result.totalRequestCount)
    }

    // Edge cases

    @Test
    fun `create should handle all failures`() {
        val timings = listOf(
            createTiming("https://example.com", success = false, statusCode = 500, errorType = ErrorType.HTTP_SERVER_ERROR, callMs = 100.0),
            createTiming("https://example.com", success = false, statusCode = 500, errorType = ErrorType.HTTP_SERVER_ERROR, callMs = 200.0)
        )

        val results = TestCaseResult.create(timings)

        assertEquals(2, results[0].totalRequestCount)
        assertEquals(0, results[0].successRequestCount)
        assertEquals(2, results[0].errorRequestCount)
        assertEquals(MetricStatistics.EMPTY, results[0].duration)
    }

    @Test
    fun `create should handle null status codes`() {
        val timings = listOf(
            createTiming("https://example.com", success = false, statusCode = null, errorType = ErrorType.DNS_FAILURE),
            createTiming("https://example.com", success = true, statusCode = 200)
        )

        val results = TestCaseResult.create(timings)

        assertEquals(1, results[0].statusCodeCounts.size)
        assertEquals(1, results[0].statusCodeCounts[200])
    }
}
