package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.entity.ExecutionMode
import com.github.mikeandv.pingwatch.entity.ResponseData
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseResult
import com.github.mikeandv.pingwatch.entity.TestEvent
import com.github.mikeandv.pingwatch.entity.UrlAvgMetrics
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.*
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume


suspend fun measureResponseTimeV2(
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
        events.tryEmit(TestEvent.RequestCompleted)
        if (cont.isActive) cont.resume(data)
    }

    cont.invokeOnCancellation {
        call.cancel()
    }

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


suspend fun runR(testCase: TestCase, cancelFlag: () -> Boolean): List<TestCaseResult> {
    testCase.events.emit(
        TestEvent.Started(
            totalRequests = if (testCase.runType == RunType.COUNT)
                testCase.totalRequests()
            else null,
            durationMs = if (testCase.runType == RunType.DURATION)
                testCase.maxDuration()
            else null
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


suspend fun runByCount(testCase: TestCase, cancelFlag: () -> Boolean): List<ResponseData> =
    when (testCase.executionMode) {

        ExecutionMode.SEQUENTIAL -> {
            val result = mutableListOf<ResponseData>()

            for ((url, param) in testCase.urls) {
                repeat(param.countValue.toInt()) {
                    if (cancelFlag()) return result
                    result += measureResponseTimeV2(
                        testCase.settings.okHttpClient,
                        url,
                        testCase.events
                    )
                }
            }
            result
        }

        ExecutionMode.PARALLEL -> coroutineScope {
            val dispatcher = Dispatchers.IO.limitedParallelism(testCase.parallelism)

            val queue = Channel<String>(capacity = testCase.parallelism * 4)
            val results = ConcurrentLinkedQueue<ResponseData>()

            val producer = launch {
                try {
                    for ((url, param) in testCase.urls) {
                        repeat(param.countValue.toInt()) {
                            if (!isActive || cancelFlag()) return@launch
                            queue.send(url)
                        }
                    }
                } finally {
                    queue.close()
                }
            }

            val workers = List(testCase.parallelism) {
                launch(dispatcher) {
                    for (url in queue) {
                        if (!isActive || cancelFlag()) break

                        val response = measureResponseTimeV2(
                            testCase.settings.okHttpClient,
                            url,
                            testCase.events
                        )

                        results.add(response)
                    }
                }
            }

            try {
                producer.join()
                workers.joinAll()
                results.toList()
            } catch (e: CancellationException) {
                producer.cancel()
                workers.forEach { it.cancel() }
                results.toList()
            }
        }
    }


suspend fun runByDuration(testCase: TestCase, cancelFlag: () -> Boolean): List<ResponseData> {
    return when (testCase.executionMode) {
        ExecutionMode.SEQUENTIAL -> {
            val result = mutableListOf<ResponseData>()
            val start = System.currentTimeMillis()

            while (System.currentTimeMillis() - start < testCase.maxDuration()) {
                for ((url, _) in testCase.urls) {
                    if (cancelFlag()) return result
                    result += measureResponseTimeV2(
                        testCase.settings.okHttpClient,
                        url,
                        testCase.events
                    )
                }
            }
            result
        }

        ExecutionMode.PARALLEL -> coroutineScope {

            val dispatcher = Dispatchers.IO.limitedParallelism(testCase.parallelism)
            val result = ConcurrentLinkedQueue<ResponseData>()
            val start = System.currentTimeMillis()

            val jobs = testCase.urls.map { (url, param) ->
                async(dispatcher) {
                    while (System.currentTimeMillis() - start < param.durationValue) {
                        if (cancelFlag()) break
                        result.add(
                            measureResponseTimeV2(
                                testCase.settings.okHttpClient,
                                url,
                                testCase.events
                            )
                        )
                    }
                }
            }

            jobs.awaitAll()
            result.toList()
        }
    }
}

