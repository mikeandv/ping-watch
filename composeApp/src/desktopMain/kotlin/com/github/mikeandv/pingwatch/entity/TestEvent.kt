package com.github.mikeandv.pingwatch.entity

sealed interface TestEvent {
    data class Started(
        val totalRequests: Long?,
        val durationMs: Long?
    ) : TestEvent

    object RequestCompleted : TestEvent
    object Finished : TestEvent
}
