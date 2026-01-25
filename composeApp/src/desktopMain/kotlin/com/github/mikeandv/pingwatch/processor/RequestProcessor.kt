package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.domain.*
import com.github.mikeandv.pingwatch.domain.ExecutionMode
import com.github.mikeandv.pingwatch.domain.RunType
import com.github.mikeandv.pingwatch.result.TestCaseResult
import com.github.mikeandv.pingwatch.result.UrlAvgMetrics
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.*
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

suspend fun runR(testCase: TestCase, cancelFlag: () -> Boolean): List<TestCaseResult> {
    testCase.events.emit(
        TestEvent.Started(
            totalRequests = if (testCase.runType == RunType.COUNT) testCase.totalRequests() else null,
            durationMs = if (testCase.runType == RunType.DURATION) testCase.maxDuration() else null
        )
    )
    testCase.settings.agg.clear()

    val rawResult = when (testCase.runType) {
        RunType.COUNT -> runByCount(testCase, cancelFlag)
        RunType.DURATION -> runByDuration(testCase, cancelFlag)
    }

    testCase.events.emit(TestEvent.Finished)
    val metricsByUrl: Map<String, UrlAvgMetrics> = testCase.settings.agg.snapshot()
    return TestCaseResult.create(rawResult, metricsByUrl)
}

private suspend fun TestCase.makeRequest(url: String): ResponseData =
    measureResponseTime(settings.okHttpClient, url, events)

private suspend fun measureResponseTime(
    client: OkHttpClient,
    url: String,
    events: MutableSharedFlow<TestEvent>
): ResponseData = suspendCancellableCoroutine { cont ->
    val request = Request.Builder().url(url).build()
    val startTime = System.currentTimeMillis()
    val call = client.newCall(request)
    val completed = AtomicBoolean(false)

    fun finishOnce(data: ResponseData) {
        if (!completed.compareAndSet(false, true)) return
        events.tryEmit(TestEvent.RequestCompleted(url))
        if (cont.isActive) cont.resume(data)
    }

    cont.invokeOnCancellation { call.cancel() }

    call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            finishOnce(ResponseData(url, -1, -1L, e.message ?: ""))
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                val duration = System.currentTimeMillis() - startTime
                finishOnce(ResponseData(url, it.code, duration, ""))
            }
        }
    })
}

private suspend fun runByCount(testCase: TestCase, cancelFlag: () -> Boolean): List<ResponseData> =
    when (testCase.executionMode) {
        ExecutionMode.SEQUENTIAL -> runByCountSequential(testCase, cancelFlag)
        ExecutionMode.PARALLEL -> runByCountParallel(testCase, cancelFlag)
    }

private suspend fun runByCountSequential(
    testCase: TestCase,
    cancelFlag: () -> Boolean
): List<ResponseData> {
    val result = mutableListOf<ResponseData>()
    for ((url, param) in testCase.urls) {
        repeat(param.countValue.toInt()) {
            if (cancelFlag()) return result
            result += testCase.makeRequest(url)
        }
    }
    return result
}

private suspend fun runByCountParallel(
    testCase: TestCase,
    cancelFlag: () -> Boolean
): List<ResponseData> = coroutineScope {
    val dispatcher = Dispatchers.IO.limitedParallelism(testCase.parallelism)
    val queue = Channel<String>(capacity = testCase.parallelism * 4)
    val results = ConcurrentLinkedQueue<ResponseData>()

    val producer = launch {
        try {
            produceInterleavedUrls(testCase, cancelFlag, queue)
        } finally {
            queue.close()
        }
    }

    val workers = launchWorkers(testCase, dispatcher, queue, results, cancelFlag)

    awaitCompletion(producer, workers, results)
}

private suspend fun CoroutineScope.produceInterleavedUrls(
    testCase: TestCase,
    cancelFlag: () -> Boolean,
    queue: Channel<String>
) {
    val remaining = testCase.urls.mapValues { it.value.countValue.toInt() }.toMutableMap()
    while (remaining.values.any { it > 0 }) {
        for ((url, count) in remaining) {
            if (count > 0) {
                if (!isActive || cancelFlag()) return
                queue.send(url)
                remaining[url] = count - 1
            }
        }
    }
}

private fun CoroutineScope.launchWorkers(
    testCase: TestCase,
    dispatcher: CoroutineDispatcher,
    queue: Channel<String>,
    results: ConcurrentLinkedQueue<ResponseData>,
    cancelFlag: () -> Boolean
): List<Job> = List(testCase.parallelism) {
    launch(dispatcher) {
        for (url in queue) {
            if (!isActive || cancelFlag()) break
            results.add(testCase.makeRequest(url))
        }
    }
}

private suspend fun awaitCompletion(
    producer: Job,
    workers: List<Job>,
    results: ConcurrentLinkedQueue<ResponseData>
): List<ResponseData> {
    try {
        producer.join()
        workers.joinAll()
    } catch (e: CancellationException) {
        producer.cancel()
        workers.forEach { it.cancel() }
    }
    return results.toList()
}

private suspend fun runByDuration(testCase: TestCase, cancelFlag: () -> Boolean): List<ResponseData> =
    when (testCase.executionMode) {
        ExecutionMode.SEQUENTIAL -> runByDurationSequential(testCase, cancelFlag)
        ExecutionMode.PARALLEL -> runByDurationParallel(testCase, cancelFlag)
    }

private suspend fun runByDurationSequential(
    testCase: TestCase,
    cancelFlag: () -> Boolean
): List<ResponseData> {
    val result = mutableListOf<ResponseData>()
    val start = System.currentTimeMillis()

    while (System.currentTimeMillis() - start < testCase.maxDuration()) {
        for ((url, _) in testCase.urls) {
            if (cancelFlag()) return result
            result += testCase.makeRequest(url)
        }
    }
    return result
}

private suspend fun runByDurationParallel(
    testCase: TestCase,
    cancelFlag: () -> Boolean
): List<ResponseData> = coroutineScope {
    val dispatcher = Dispatchers.IO.limitedParallelism(testCase.parallelism)
    val results = ConcurrentLinkedQueue<ResponseData>()
    val start = System.currentTimeMillis()

    val jobs = testCase.urls.map { (url, param) ->
        async(dispatcher) {
            while (System.currentTimeMillis() - start < param.durationValue) {
                if (cancelFlag()) break
                results.add(testCase.makeRequest(url))
            }
        }
    }

    jobs.awaitAll()
    results.toList()
}
