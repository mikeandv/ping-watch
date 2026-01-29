package com.github.mikeandv.pingwatch.domain

import com.github.mikeandv.pingwatch.listener.TimingEventListenerFactory
import com.github.mikeandv.pingwatch.result.UrlAvgAggregator
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

data class TestCaseSettings(
    val maxFileSize: Int = DEFAULT_MAX_FILE_SIZE,
    val maxLinesLimit: Int = DEFAULT_MAX_LINE_LIMIT,
    val allowedFileExtensions: List<String> = DEFAULT_FILE_EXTENSIONS,
    val earlyStopThreshold: Int = DEFAULT_EARLY_STOP_THRESHOLD,
    val dispatcherMaxRequests: Int = DEFAULT_MAX_REQUESTS,
    val dispatcherMaxRequestsPerHost: Int = DEFAULT_MAX_REQUESTS_PER_HOST
) {
    fun createHttpClient(agg: UrlAvgAggregator): OkHttpClient {
        val dispatcher = Dispatcher().apply {
            maxRequests = dispatcherMaxRequests
            maxRequestsPerHost = dispatcherMaxRequestsPerHost
        }

        return OkHttpClient.Builder()
            .cache(null)
            .dispatcher(dispatcher)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .eventListenerFactory(TimingEventListenerFactory { agg.add(it) })
            .build()
    }

    companion object {
        val DEFAULT_FILE_EXTENSIONS = listOf("txt")
        const val DEFAULT_MAX_FILE_SIZE = 5
        const val DEFAULT_MAX_LINE_LIMIT = 20
        const val DEFAULT_MAX_REQUESTS = 256
        const val DEFAULT_MAX_REQUESTS_PER_HOST = 256
        const val DEFAULT_EARLY_STOP_THRESHOLD = 5
    }
}
