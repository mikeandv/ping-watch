package com.github.mikeandv.pingwatch.entity

import com.github.mikeandv.pingwatch.aggregator.UrlAvgAggregator
import com.github.mikeandv.pingwatch.listener.TimingEventListenerFactory
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class TestCaseSettings private constructor(
    val maxFileSize: Int,
    val maxLinesLimit: Int,
    val allowedFileExtensions: List<String>,
    val agg: UrlAvgAggregator,
    val okHttpClient: OkHttpClient
) {
    companion object {
        val DEFAULT_FILE_EXTENSIONS = listOf("txt")

        fun createDefaultSettings(): TestCaseSettings {
            val agg = UrlAvgAggregator()

            val client = OkHttpClient.Builder()
                .cache(null)
                .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                .eventListenerFactory(TimingEventListenerFactory { agg.add(it) })
                .build()
            return TestCaseSettings(
                5,
                20,
                DEFAULT_FILE_EXTENSIONS,
                agg,
                client
            )
        }
    }

    fun copy(
        maxFileSize: Int = this.maxFileSize,
        maxLinesLimit: Int = this.maxLinesLimit,
        allowedFileExtensions: List<String> = this.allowedFileExtensions,
        agg: UrlAvgAggregator = this.agg,
        okHttpClient: OkHttpClient = this.okHttpClient
    ): TestCaseSettings {
        return TestCaseSettings(maxFileSize, maxLinesLimit, allowedFileExtensions, agg, okHttpClient)
    }
}