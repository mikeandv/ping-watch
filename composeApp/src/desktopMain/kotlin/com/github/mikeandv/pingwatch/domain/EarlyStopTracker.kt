package com.github.mikeandv.pingwatch.domain

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class EarlyStopTracker(threshold: Int) {

    @Volatile
    private var threshold: Int = threshold

    private val pendingRequests = ConcurrentHashMap<String, AtomicInteger>()
    private val consecutiveErrors = ConcurrentHashMap<String, AtomicInteger>()
    private val stoppedUrls = ConcurrentHashMap.newKeySet<String>()

    fun updateThreshold(newThreshold: Int) {
        threshold = newThreshold
    }

    fun tryAcquire(url: String): Boolean {
        if (stoppedUrls.contains(url)) return false

        val pending = pendingRequests.computeIfAbsent(url) { AtomicInteger(0) }
        val errors = consecutiveErrors[url]?.get() ?: 0

        if (pending.get() + errors >= threshold) {
            return false
        }

        pending.incrementAndGet()
        return true
    }

    fun record(url: String, errorType: ErrorType) {
        pendingRequests[url]?.decrementAndGet()

        if (stoppedUrls.contains(url)) return

        if (errorType.isCritical()) {
            val counter = consecutiveErrors.computeIfAbsent(url) { AtomicInteger(0) }
            if (counter.incrementAndGet() >= threshold) {
                stoppedUrls.add(url)
            }
        } else {
            consecutiveErrors[url]?.set(0)
        }
    }

    fun shouldStop(url: String): Boolean = stoppedUrls.contains(url)

    fun clear() {
        pendingRequests.clear()
        consecutiveErrors.clear()
        stoppedUrls.clear()
    }
}