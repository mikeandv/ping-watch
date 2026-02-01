package com.github.mikeandv.pingwatch.domain

import com.github.mikeandv.pingwatch.listener.TimingEventListenerFactory
import com.github.mikeandv.pingwatch.result.UrlAvgAggregator
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

data class TestCaseSettings(
    val urlPattern: Regex = DEFAULT_URL_PATTERN,
    val maxFileSize: Int = DEFAULT_MAX_FILE_SIZE,
    val maxLinesLimit: Int = DEFAULT_MAX_LINE_LIMIT,
    val allowedFileExtensions: List<String> = DEFAULT_FILE_EXTENSIONS,
    val earlyStopThreshold: Int = DEFAULT_EARLY_STOP_THRESHOLD,
    val dispatcherMaxRequests: Int = DEFAULT_MAX_DISPATCHER_REQUESTS,
    val dispatcherMaxRequestsPerHost: Int = DEFAULT_MAX_DISPATCHER_REQUESTS_PER_HOST,
    val minCommonInput: Int = DEFAULT_MIN_COMMON_INPUT,
    val maxCountInput: Int = DEFAULT_MAX_COUNT_INPUT,
    val maxParallelismInput: Int = DEFAULT_MAX_PARALLELISM_INPUT,
    val maxFileSizeInput: Int = DEFAULT_MAX_FILE_SIZE_INPUT,
    val maxLineLimitInput: Int = DEFAULT_MAX_LINE_LIMIT_INPUT,
    val maxEarlyStopThresholdInput: Int = DEFAULT_MAX_EARLY_STOP_THRESHOLD_INPUT,
    val maxDispatcherRequestsInput: Int = DEFAULT_MAX_DISPATCHER_REQUESTS_INPUT
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
        val DEFAULT_URL_PATTERN = Regex("^https?://(localhost|\\d{1,3}(\\.\\d{1,3}){3}|([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,})(:\\d+)?(/\\S*)?$")
        val DEFAULT_FILE_EXTENSIONS = listOf("txt")
        const val DEFAULT_MAX_FILE_SIZE = 5
        const val DEFAULT_MAX_LINE_LIMIT = 20
        const val DEFAULT_MAX_DISPATCHER_REQUESTS = 256
        const val DEFAULT_MAX_DISPATCHER_REQUESTS_PER_HOST = 256
        const val DEFAULT_EARLY_STOP_THRESHOLD = 5
        const val DEFAULT_MIN_COMMON_INPUT =1
        const val DEFAULT_MAX_COUNT_INPUT = 10000
        const val DEFAULT_MAX_PARALLELISM_INPUT = 64
        const val DEFAULT_MAX_FILE_SIZE_INPUT = 100
        const val DEFAULT_MAX_LINE_LIMIT_INPUT = 1000
        const val DEFAULT_MAX_EARLY_STOP_THRESHOLD_INPUT = 100
        const val DEFAULT_MAX_DISPATCHER_REQUESTS_INPUT = 256
    }
}
