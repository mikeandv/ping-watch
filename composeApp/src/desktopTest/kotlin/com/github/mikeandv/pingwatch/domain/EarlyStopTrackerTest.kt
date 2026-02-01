package com.github.mikeandv.pingwatch.domain

import kotlin.test.*

class EarlyStopTrackerTest {

    // tryAcquire tests

    @Test
    fun `tryAcquire should return true for new URL`() {
        val tracker = EarlyStopTracker(5)
        assertTrue(tracker.tryAcquire("https://example.com"))
    }

    @Test
    fun `tryAcquire should return true when below threshold`() {
        val tracker = EarlyStopTracker(3)
        val url = "https://example.com"

        assertTrue(tracker.tryAcquire(url))
        assertTrue(tracker.tryAcquire(url))
    }

    @Test
    fun `tryAcquire should return false when at threshold`() {
        val tracker = EarlyStopTracker(2)
        val url = "https://example.com"

        assertTrue(tracker.tryAcquire(url))
        assertTrue(tracker.tryAcquire(url))
        assertFalse(tracker.tryAcquire(url))
    }

    @Test
    fun `tryAcquire should return false for stopped URL`() {
        val tracker = EarlyStopTracker(1)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.DNS_FAILURE)

        assertFalse(tracker.tryAcquire(url))
    }

    @Test
    fun `tryAcquire should track multiple URLs independently`() {
        val tracker = EarlyStopTracker(2)
        val url1 = "https://example1.com"
        val url2 = "https://example2.com"

        assertTrue(tracker.tryAcquire(url1))
        assertTrue(tracker.tryAcquire(url1))
        assertFalse(tracker.tryAcquire(url1))

        assertTrue(tracker.tryAcquire(url2))
        assertTrue(tracker.tryAcquire(url2))
    }

    // record tests

    @Test
    fun `record should decrement pending count`() {
        val tracker = EarlyStopTracker(2)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.tryAcquire(url)
        assertFalse(tracker.tryAcquire(url))

        tracker.record(url, ErrorType.NONE)
        assertTrue(tracker.tryAcquire(url))
    }

    @Test
    fun `record should increment error count for critical errors`() {
        val tracker = EarlyStopTracker(2)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.DNS_FAILURE)

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.SSL_ERROR)

        assertTrue(tracker.shouldStop(url))
    }

    @Test
    fun `record should reset error count for non-critical errors`() {
        val tracker = EarlyStopTracker(3)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.DNS_FAILURE)

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.DNS_FAILURE)

        // Non-critical error resets count
        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.NONE)

        assertFalse(tracker.shouldStop(url))
    }

    @Test
    fun `record should not process already stopped URL`() {
        val tracker = EarlyStopTracker(1)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.DNS_FAILURE)
        assertTrue(tracker.shouldStop(url))

        // This should not throw or cause issues
        tracker.record(url, ErrorType.NONE)
        assertTrue(tracker.shouldStop(url))
    }

    // shouldStop tests

    @Test
    fun `shouldStop should return false for new URL`() {
        val tracker = EarlyStopTracker(5)
        assertFalse(tracker.shouldStop("https://example.com"))
    }

    @Test
    fun `shouldStop should return true after threshold critical errors`() {
        val tracker = EarlyStopTracker(2)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.CONNECTION_REFUSED)

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.HOST_UNREACHABLE)

        assertTrue(tracker.shouldStop(url))
    }

    @Test
    fun `shouldStop should return false for non-critical errors`() {
        val tracker = EarlyStopTracker(2)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.TIMEOUT)

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.NETWORK_ERROR)

        assertFalse(tracker.shouldStop(url))
    }

    @Test
    fun `shouldStop should return true for HTTP_CRITICAL_ERROR`() {
        val tracker = EarlyStopTracker(2)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.HTTP_CRITICAL_ERROR)

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.HTTP_CRITICAL_ERROR)

        assertTrue(tracker.shouldStop(url))
    }

    // updateThreshold tests

    @Test
    fun `updateThreshold should change threshold`() {
        val tracker = EarlyStopTracker(5)
        val url = "https://example.com"

        tracker.updateThreshold(1)

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.DNS_FAILURE)

        assertTrue(tracker.shouldStop(url))
    }

    // clear tests

    @Test
    fun `clear should reset all state`() {
        val tracker = EarlyStopTracker(1)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.DNS_FAILURE)
        assertTrue(tracker.shouldStop(url))

        tracker.clear()

        assertFalse(tracker.shouldStop(url))
        assertTrue(tracker.tryAcquire(url))
    }

    // Critical error types tests

    @Test
    fun `DNS_FAILURE should be critical`() {
        val tracker = EarlyStopTracker(1)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.DNS_FAILURE)

        assertTrue(tracker.shouldStop(url))
    }

    @Test
    fun `SSL_ERROR should be critical`() {
        val tracker = EarlyStopTracker(1)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.SSL_ERROR)

        assertTrue(tracker.shouldStop(url))
    }

    @Test
    fun `HOST_UNREACHABLE should be critical`() {
        val tracker = EarlyStopTracker(1)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.HOST_UNREACHABLE)

        assertTrue(tracker.shouldStop(url))
    }

    @Test
    fun `CONNECTION_REFUSED should be critical`() {
        val tracker = EarlyStopTracker(1)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.CONNECTION_REFUSED)

        assertTrue(tracker.shouldStop(url))
    }

    @Test
    fun `TIMEOUT should not be critical`() {
        val tracker = EarlyStopTracker(1)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.TIMEOUT)

        assertFalse(tracker.shouldStop(url))
    }

    @Test
    fun `NETWORK_ERROR should not be critical`() {
        val tracker = EarlyStopTracker(1)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.NETWORK_ERROR)

        assertFalse(tracker.shouldStop(url))
    }

    @Test
    fun `HTTP_CLIENT_ERROR should not be critical`() {
        val tracker = EarlyStopTracker(1)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.HTTP_CLIENT_ERROR)

        assertFalse(tracker.shouldStop(url))
    }

    @Test
    fun `HTTP_SERVER_ERROR should not be critical`() {
        val tracker = EarlyStopTracker(1)
        val url = "https://example.com"

        tracker.tryAcquire(url)
        tracker.record(url, ErrorType.HTTP_SERVER_ERROR)

        assertFalse(tracker.shouldStop(url))
    }
}
