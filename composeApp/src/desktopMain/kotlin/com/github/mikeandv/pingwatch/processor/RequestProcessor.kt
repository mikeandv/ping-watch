package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.domain.ExecutionMode
import com.github.mikeandv.pingwatch.domain.RunType
import com.github.mikeandv.pingwatch.domain.TestCase
import com.github.mikeandv.pingwatch.domain.TestEvent
import com.github.mikeandv.pingwatch.result.TestCaseResult
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.*
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

suspend fun runR(testCase: TestCase, cancelFlag: () -> Boolean): List<TestCaseResult> {
    testCase.events.emit(
        TestEvent.Started(
            totalRequests = if (testCase.runType == RunType.COUNT) testCase.totalRequests() else null,
            durationMs = if (testCase.runType == RunType.DURATION) testCase.maxDuration() else null
        )
    )
    testCase.agg.clear()

    when (testCase.runType) {
        RunType.COUNT -> runByCount(testCase, cancelFlag)
        RunType.DURATION -> runByDuration(testCase, cancelFlag)
    }

    testCase.events.emit(TestEvent.Finished)

    // Get all timings collected by TimingEventListener
    val allTimings = testCase.agg.getAllTimings()
    return TestCaseResult.create(allTimings)
}

private suspend fun TestCase.makeRequest(url: String) {
    executeRequest(okHttpClient, url, events)
}

private suspend fun executeRequest(
    client: OkHttpClient,
    url: String,
    events: MutableSharedFlow<TestEvent>
): Unit = suspendCancellableCoroutine { cont ->
    val request = Request.Builder().url(url).build()
    val call = client.newCall(request)
    val completed = AtomicBoolean(false)

    fun finishOnce() {
        if (!completed.compareAndSet(false, true)) return
        events.tryEmit(TestEvent.RequestCompleted(url))
        if (cont.isActive) cont.resume(Unit)
    }

    cont.invokeOnCancellation { call.cancel() }

    call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            finishOnce()
        }

        override fun onResponse(call: Call, response: Response) {
            response.use { finishOnce() }
        }
    })
}

private suspend fun runByCount(testCase: TestCase, cancelFlag: () -> Boolean) =
    when (testCase.executionMode) {
        ExecutionMode.SEQUENTIAL -> runByCountSequential(testCase, cancelFlag)
        ExecutionMode.PARALLEL -> runByCountParallel(testCase, cancelFlag)
    }

private suspend fun runByCountSequential(
    testCase: TestCase,
    cancelFlag: () -> Boolean
) {
    for ((url, param) in testCase.urls) {
        var remaining = param.countValue.toInt()
        while (remaining > 0) {
            if (cancelFlag()) return
            if (testCase.agg.shouldStopEarly(url)) break
            testCase.makeRequest(url)
            remaining--
        }
    }
}

private suspend fun runByCountParallel(
    testCase: TestCase,
    cancelFlag: () -> Boolean
) = coroutineScope {
    val dispatcher = Dispatchers.IO.limitedParallelism(testCase.parallelism)
    val queue = Channel<String>(capacity = testCase.parallelism * 4)

    val producer = launch {
        try {
            produceInterleavedUrls(testCase, cancelFlag, queue)
        } finally {
            queue.close()
        }
    }

    val workers = launchWorkers(testCase, dispatcher, queue, cancelFlag)

    awaitCompletion(producer, workers)
}

private suspend fun CoroutineScope.produceInterleavedUrls(
    testCase: TestCase,
    cancelFlag: () -> Boolean,
    queue: Channel<String>
) {
    val remaining = testCase.urls.mapValues { it.value.countValue.toInt() }.toMutableMap()
    while (remaining.values.any { it > 0 }) {
        var madeProgress = false
        for ((url, count) in remaining) {
            if (count > 0) {
                if (!isActive || cancelFlag()) return
                if (testCase.agg.shouldStopEarly(url)) {
                    remaining[url] = 0
                    continue
                }
                if (!testCase.agg.tryAcquireSlot(url)) {
                    continue
                }
                queue.send(url)
                remaining[url] = count - 1
                madeProgress = true
            }
        }
        if (!madeProgress && remaining.values.any { it > 0 }) {
            delay(10)
        }
    }
}

private fun CoroutineScope.launchWorkers(
    testCase: TestCase,
    dispatcher: CoroutineDispatcher,
    queue: Channel<String>,
    cancelFlag: () -> Boolean
): List<Job> = List(testCase.parallelism) {
    launch(dispatcher) {
        for (url in queue) {
            if (!isActive || cancelFlag()) break
            if (testCase.agg.shouldStopEarly(url)) continue
            testCase.makeRequest(url)
        }
    }
}

private suspend fun awaitCompletion(
    producer: Job,
    workers: List<Job>
) {
    try {
        producer.join()
        workers.joinAll()
    } catch (e: CancellationException) {
        producer.cancel()
        workers.forEach { it.cancel() }
    }
}

private suspend fun runByDuration(testCase: TestCase, cancelFlag: () -> Boolean) =
    when (testCase.executionMode) {
        ExecutionMode.SEQUENTIAL -> runByDurationSequential(testCase, cancelFlag)
        ExecutionMode.PARALLEL -> runByDurationParallel(testCase, cancelFlag)
    }

private suspend fun runByDurationSequential(
    testCase: TestCase,
    cancelFlag: () -> Boolean
) {
    val start = System.currentTimeMillis()

    while (System.currentTimeMillis() - start < testCase.maxDuration()) {
        val activeUrls = testCase.urls.keys.filterNot { testCase.agg.shouldStopEarly(it) }
        if (activeUrls.isEmpty()) return
        for (url in activeUrls) {
            if (cancelFlag()) return
            testCase.makeRequest(url)
        }
    }
}

private suspend fun runByDurationParallel(
    testCase: TestCase,
    cancelFlag: () -> Boolean
) = coroutineScope {
    val dispatcher = Dispatchers.IO.limitedParallelism(testCase.parallelism)
    val start = System.currentTimeMillis()

    val jobs = testCase.urls.map { (url, param) ->
        async(dispatcher) {
            while (System.currentTimeMillis() - start < param.durationValue) {
                if (cancelFlag()) break
                if (testCase.agg.shouldStopEarly(url)) break
                if (!testCase.agg.tryAcquireSlot(url)) {
                    delay(10)
                    continue
                }
                testCase.makeRequest(url)
            }
        }
    }

    jobs.awaitAll()
}
