package com.github.mikeandv.pingwatch.result

import com.github.mikeandv.pingwatch.domain.ErrorType
import kotlin.test.*

class UrlAvgAggregatorTest {

    private fun createTiming(
        url: String,
        success: Boolean = true,
        statusCode: Int? = 200,
        errorType: ErrorType = ErrorType.NONE,
        callMs: Double = 100.0
    ) = RequestTimings(
        url = url,
        success = success,
        statusCode = statusCode,
        error = null,
        errorType = errorType,
        callMs = callMs,
        dnsMs = null,
        connectMs = null,
        tlsMs = null,
        requestHeadersMs = null,
        requestBodyMs = null,
        responseHeadersMs = null,
        responseBodyMs = null
    )

    // add tests

    @Test
    fun `add should store timing`() {
        val aggregator = UrlAvgAggregator(5)
        val timing = createTiming("https://example.com")

        aggregator.add(timing)

        assertEquals(1, aggregator.getAllTimings().size)
        assertEquals(timing, aggregator.getAllTimings()[0])
    }

    @Test
    fun `add should store multiple timings`() {
        val aggregator = UrlAvgAggregator(5)

        aggregator.add(createTiming("https://example1.com"))
        aggregator.add(createTiming("https://example2.com"))
        aggregator.add(createTiming("https://example1.com"))

        assertEquals(3, aggregator.getAllTimings().size)
    }

    @Test
    fun `add should delegate to EarlyStopTracker for error recording`() {
        val aggregator = UrlAvgAggregator(2)
        val url = "https://example.com"

        aggregator.tryAcquireSlot(url)
        aggregator.add(createTiming(url, success = false, statusCode = null, errorType = ErrorType.DNS_FAILURE))
        aggregator.tryAcquireSlot(url)
        aggregator.add(createTiming(url, success = false, statusCode = null, errorType = ErrorType.SSL_ERROR))

        assertTrue(aggregator.shouldStopEarly(url))
    }

    // tryAcquireSlot tests

    @Test
    fun `tryAcquireSlot should return true when below threshold`() {
        val aggregator = UrlAvgAggregator(3)
        val url = "https://example.com"

        assertTrue(aggregator.tryAcquireSlot(url))
        assertTrue(aggregator.tryAcquireSlot(url))
    }

    @Test
    fun `tryAcquireSlot should return false when at threshold`() {
        val aggregator = UrlAvgAggregator(2)
        val url = "https://example.com"

        assertTrue(aggregator.tryAcquireSlot(url))
        assertTrue(aggregator.tryAcquireSlot(url))
        assertFalse(aggregator.tryAcquireSlot(url))
    }

    @Test
    fun `tryAcquireSlot should return false when URL is stopped`() {
        val aggregator = UrlAvgAggregator(1)
        val url = "https://example.com"

        aggregator.tryAcquireSlot(url)
        aggregator.add(createTiming(url, success = false, statusCode = null, errorType = ErrorType.DNS_FAILURE))

        assertFalse(aggregator.tryAcquireSlot(url))
    }

    // shouldStopEarly tests

    @Test
    fun `shouldStopEarly should return false initially`() {
        val aggregator = UrlAvgAggregator(5)
        assertFalse(aggregator.shouldStopEarly("https://example.com"))
    }

    @Test
    fun `shouldStopEarly should return true after critical errors reach threshold`() {
        val aggregator = UrlAvgAggregator(2)
        val url = "https://example.com"

        aggregator.tryAcquireSlot(url)
        aggregator.add(createTiming(url, success = false, errorType = ErrorType.HOST_UNREACHABLE))
        aggregator.tryAcquireSlot(url)
        aggregator.add(createTiming(url, success = false, errorType = ErrorType.CONNECTION_REFUSED))

        assertTrue(aggregator.shouldStopEarly(url))
    }

    // getAllTimings tests

    @Test
    fun `getAllTimings should return empty list initially`() {
        val aggregator = UrlAvgAggregator(5)
        assertTrue(aggregator.getAllTimings().isEmpty())
    }

    @Test
    fun `getAllTimings should return copy of timings`() {
        val aggregator = UrlAvgAggregator(5)
        val timing = createTiming("https://example.com", callMs = 150.0)

        aggregator.add(timing)

        val timings = aggregator.getAllTimings()
        assertEquals(1, timings.size)
        assertEquals(150.0, timings[0].callMs)
    }

    @Test
    fun `getAllTimings should return all added timings`() {
        val aggregator = UrlAvgAggregator(10)

        aggregator.add(createTiming("https://example1.com", callMs = 100.0))
        aggregator.add(createTiming("https://example2.com", callMs = 200.0))
        aggregator.add(createTiming("https://example1.com", callMs = 150.0))

        val timings = aggregator.getAllTimings()
        assertEquals(3, timings.size)
    }

    // clear tests

    @Test
    fun `clear should clear timings`() {
        val aggregator = UrlAvgAggregator(5)

        aggregator.add(createTiming("https://example.com"))
        assertEquals(1, aggregator.getAllTimings().size)

        aggregator.clear()
        assertEquals(0, aggregator.getAllTimings().size)
    }

    @Test
    fun `clear should clear tracker state`() {
        val aggregator = UrlAvgAggregator(1)
        val url = "https://example.com"

        aggregator.tryAcquireSlot(url)
        aggregator.add(createTiming(url, success = false, errorType = ErrorType.DNS_FAILURE))
        assertTrue(aggregator.shouldStopEarly(url))

        aggregator.clear()

        assertFalse(aggregator.shouldStopEarly(url))
        assertTrue(aggregator.tryAcquireSlot(url))
    }

    // updateEarlyStopThreshold tests

    @Test
    fun `updateEarlyStopThreshold should change threshold`() {
        val aggregator = UrlAvgAggregator(5)
        val url = "https://example.com"

        aggregator.updateEarlyStopThreshold(1)

        aggregator.tryAcquireSlot(url)
        aggregator.add(createTiming(url, success = false, errorType = ErrorType.DNS_FAILURE))

        assertTrue(aggregator.shouldStopEarly(url))
    }

    // Multiple URLs tests

    @Test
    fun `should track multiple URLs independently`() {
        val aggregator = UrlAvgAggregator(2)
        val url1 = "https://example1.com"
        val url2 = "https://example2.com"

        // Stop url1
        aggregator.tryAcquireSlot(url1)
        aggregator.add(createTiming(url1, success = false, errorType = ErrorType.DNS_FAILURE))
        aggregator.tryAcquireSlot(url1)
        aggregator.add(createTiming(url1, success = false, errorType = ErrorType.SSL_ERROR))

        assertTrue(aggregator.shouldStopEarly(url1))
        assertFalse(aggregator.shouldStopEarly(url2))
        assertTrue(aggregator.tryAcquireSlot(url2))
    }
}
