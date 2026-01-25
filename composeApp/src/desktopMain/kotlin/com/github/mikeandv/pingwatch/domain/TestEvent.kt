package com.github.mikeandv.pingwatch.domain

sealed interface TestEvent {
    data class Started(
        val totalRequests: Long?,
        val durationMs: Long?
    ) : TestEvent

    data class RequestCompleted(val url: String) : TestEvent
    object Finished : TestEvent
}
