package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseResult
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis

val client = OkHttpClient()

fun measureResponseTime(url: String): ResponseData {
    var statusCode: Int
    var requestDuration: Long

    try {
        requestDuration = measureTimeMillis {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                statusCode = response.code
            }
        }
    } catch (e: Exception) {
//        println("Error making request to $url: ${e.message}")
        statusCode = -1
        requestDuration = -1L
    }
    return ResponseData(url, statusCode, requestDuration)
}


suspend fun runByCount(
    count: Long,
    urls: List<String>,
    executionCounter: AtomicLong,
    cancelFlag: () -> Boolean
): List<ResponseData> =
    withContext(Dispatchers.IO) {
        val resultTemp = ConcurrentLinkedQueue<ResponseData>()

        val jobs = urls.map { url ->
            async {
                repeat(count.toInt()) {

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

suspend fun runByDuration(durationMillis: Long, urls: List<String>, cancelFlag: () -> Boolean): List<ResponseData> =
    coroutineScope {
        val startTime = System.currentTimeMillis()

        val resultTemp = ConcurrentLinkedQueue<ResponseData>()

        val jobs = urls.map { url ->
            launch(Dispatchers.IO) {
                while (System.currentTimeMillis() - startTime < durationMillis) {
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
//            delay(durationMillis)
        } catch (e: CancellationException) {
            println("Jobs canceled: ${e.message}")
        } finally {
            // Завершаем все корутины
            jobs.forEach { it.cancelAndJoin() }
        }

        return@coroutineScope resultTemp.toList()
    }

suspend fun runR(testCase: TestCase, cancelFlag: () -> Boolean): List<TestCaseResult> {

    val result = when (testCase.runType) {
        RunType.DURATION -> runByDuration(testCase.durationValue ?: 0, testCase.urls, cancelFlag)
        RunType.COUNT -> runByCount(
            testCase.countValue ?: 0,
            testCase.urls,
            testCase.testCaseState.getExecutionCounter(),
            cancelFlag
        )
    }

    return TestCaseResult.create(result)
}

data class ResponseData(val url: String, val statusCode: Int, val duration: Long)
