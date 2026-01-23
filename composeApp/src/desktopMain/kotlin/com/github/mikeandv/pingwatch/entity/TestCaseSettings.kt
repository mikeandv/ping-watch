package com.github.mikeandv.pingwatch.entity

import com.github.mikeandv.pingwatch.aggregator.UrlAvgAggregator
import com.github.mikeandv.pingwatch.listener.TimingEventListenerFactory
import okhttp3.OkHttpClient

class TestCaseSettings private constructor(
    val executionMode: ExecutionMode,
    val parallelism: Int,
    val agg: UrlAvgAggregator,
    val okHttpClient: OkHttpClient
) {
    companion object {
        fun createDefaultSettings(): TestCaseSettings {
            val agg = UrlAvgAggregator()

            val client = OkHttpClient.Builder()
                .eventListenerFactory(TimingEventListenerFactory { agg.add(it) })
                .build()
            return TestCaseSettings(
                ExecutionMode.SEQUENTIAL,
                8,
                agg,
                client
            )
        }
    }

    fun copy(
        executionMode: ExecutionMode = this.executionMode,
        parallelism: Int = this.parallelism,
        agg: UrlAvgAggregator = this.agg,
        okHttpClient: OkHttpClient = this.okHttpClient
    ): TestCaseSettings {
        return TestCaseSettings(executionMode, parallelism, agg, okHttpClient)
    }
}