package com.github.mikeandv.pingwatch.domain

import com.github.mikeandv.pingwatch.listener.TimingEventListenerFactory
import com.github.mikeandv.pingwatch.result.UrlAvgAggregator
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
        const val DEFAULT_MAX_FILE_SIZE = 5
        const val DEFAULT_MAX_LINE_LIMIT = 20

        fun createDefaultSettings(): TestCaseSettings {
            val agg = UrlAvgAggregator()

            val client = OkHttpClient.Builder()
                .cache(null)
                .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                .eventListenerFactory(TimingEventListenerFactory { agg.add(it) })
                .build()
            return TestCaseSettings(
                DEFAULT_MAX_FILE_SIZE,
                DEFAULT_MAX_LINE_LIMIT,
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
