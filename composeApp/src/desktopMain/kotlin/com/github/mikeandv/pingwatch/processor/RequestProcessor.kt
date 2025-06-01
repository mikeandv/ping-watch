package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseParams
import com.github.mikeandv.pingwatch.entity.TestCaseResult
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis

val client_old = OkHttpClient()

suspend fun measureResponseTimeV2(client: OkHttpClient, url: String): ResponseData =
    suspendCancellableCoroutine { continuation ->
        val request = Request.Builder().url(url).build()
        val startTime = System.currentTimeMillis()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Error making request to $url: ${e.message}")
                if (continuation.isActive) {
                    continuation.resumeWith(Result.success(ResponseData(url, -1, -1L, e.message ?: "")))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val duration = System.currentTimeMillis() - startTime
                val statusCode = response.code
                response.close()
                if (continuation.isActive) {
                    continuation.resumeWith(Result.success(ResponseData(url, statusCode, duration, "")))
                }
            }
        })
    }

fun measureResponseTime(url: String): ResponseData {
    var statusCode: Int
    var requestDuration: Long
    var errorMessage = ""

    try {
        requestDuration = measureTimeMillis {
            val request = Request.Builder().url(url).build()
            client_old.newCall(request).execute().use { response ->
                statusCode = response.code
            }
        }
    } catch (e: Exception) {
        println("Error making request to $url: ${e.message}")
        statusCode = -1
        requestDuration = -1L
        errorMessage = e.message ?: ""
    }
    return ResponseData(url, statusCode, requestDuration, errorMessage)
}


suspend fun runByCount(
    urls: Map<String, TestCaseParams>,
    executionCounter: AtomicLong,
    cancelFlag: () -> Boolean
): List<ResponseData> =
    withContext(Dispatchers.IO) {
        val resultTemp = ConcurrentLinkedQueue<ResponseData>()

        val jobs = urls.map { (url, param) ->
            async {
                repeat(param.countValue.toInt()) {

                    if (cancelFlag()) {
                        println("Job canceled ")
                        return@async
                    }
                    executionCounter.incrementAndGet()

                    val responseData = measureResponseTime(url)
                    resultTemp.add(responseData)
                }
            }
        }
        try {
            jobs.awaitAll()
        } catch (e: CancellationException) {
            println("Jobs canceled: ${e.message}")
        }

        return@withContext resultTemp.toList()
    }

suspend fun runByCountV2(
    client: OkHttpClient,
    urls: Map<String, TestCaseParams>,
    executionCounter: AtomicLong,
    cancelFlag: () -> Boolean
): List<ResponseData> = coroutineScope {
    val resultTemp = ConcurrentLinkedQueue<ResponseData>()

    val jobs = urls.map { (url, param) ->
        launch {
            repeat(param.countValue.toInt()) {
                if (cancelFlag()) {
                    println("Job canceled")
                    return@launch
                }
                executionCounter.incrementAndGet()
                val responseData = measureResponseTimeV2(client, url)
                resultTemp.add(responseData)
            }
        }
    }

    jobs.joinAll() // Дожидаемся завершения всех задач
    resultTemp.toList()
}

suspend fun runByDurationV2(
    client: OkHttpClient,
    urls: Map<String, TestCaseParams>,
    cancelFlag: () -> Boolean
): List<ResponseData> = coroutineScope {
    val startTime = System.currentTimeMillis()
    val resultTemp = ConcurrentLinkedQueue<ResponseData>()

    val jobs = urls.map { (url, param) ->
        launch {
            while (System.currentTimeMillis() - startTime < param.durationValue) {
                if (cancelFlag()) {
                    println("Job canceled")
                    return@launch
                }
                val responseData = measureResponseTimeV2(client, url)
                resultTemp.add(responseData)
            }
        }
    }

    jobs.joinAll()
    resultTemp.toList()
}

suspend fun runByDuration(urls: Map<String, TestCaseParams>, cancelFlag: () -> Boolean): List<ResponseData> =
    coroutineScope {
        val startTime = System.currentTimeMillis()
        val resultTemp = ConcurrentLinkedQueue<ResponseData>()

        val jobs = urls.map { (url, param) ->
            launch(Dispatchers.IO) {
                while (System.currentTimeMillis() - startTime < param.durationValue) {
                    if (cancelFlag()) {
                        println("Job canceled")
                        return@launch
                    }
                    val responseData = measureResponseTime(url)
                    resultTemp.add(responseData)
                }
            }
        }

        try {
            jobs.forEach { it.join() }
        } catch (e: CancellationException) {
            println("Jobs canceled: ${e.message}")
        } finally {
            jobs.forEach { it.cancelAndJoin() }
        }

        return@coroutineScope resultTemp.toList()
    }

suspend fun runR(testCase: TestCase, cancelFlag: () -> Boolean): List<TestCaseResult> {

    val result = when (testCase.runType) {
        RunType.DURATION -> runByDurationV2(
            testCase.okHttpClient,
            testCase.urls,
            cancelFlag
        )

        RunType.COUNT -> runByCountV2(
            testCase.okHttpClient,
            testCase.urls,
            testCase.testCaseState.getExecutionCounter(),
            cancelFlag
        )
    }

    return TestCaseResult.create(result)
}

data class ResponseData(val url: String, val statusCode: Int, val duration: Long, val errorMessage: String)
