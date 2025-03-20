package com.github.mikeandv.pingwatch

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

val client = OkHttpClient()

fun measureResponseTime(url: String): ResponseData {
    var statusCode: Int

    val requestDuration = measureTimeMillis {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            statusCode = response.code
        }
    }
    return ResponseData(url, statusCode, requestDuration)
}


suspend fun runByCount(count: Int, urls: List<String>, executionCounter: AtomicInteger): List<ResponseData> =
    withContext(Dispatchers.IO) {
        val resultTemp = ConcurrentLinkedQueue<ResponseData>()

        val jobs = urls.map { url ->
            async {
                repeat(count) {
                    val currentCount = executionCounter.incrementAndGet() // Последовательный инкремент
                    println("URL: $url, executionCounter: $currentCount") // 👀 Проверка

                    val responseData = measureResponseTime(url)
                    resultTemp.add(responseData)
                }
            }
        }

        jobs.awaitAll()
        return@withContext resultTemp.toList()
//    val resultTemp = ConcurrentLinkedQueue<ResponseData>()
//    urls.map { url ->
//        async {
//            resultTemp.addAll(List(count) {
//                val currentCount = executionCounter.incrementAndGet()
//                println("URL: $url, executionCounter: $currentCount")
//                executionCounter.incrementAndGet()
////                println(url)
//                measureResponseTime(url)
//            })
//        }
//    }.awaitAll()
//    return@coroutineScope resultTemp.toList()
    }

suspend fun runByDuration(durationMillis: Long, urls: List<String>): List<ResponseData> =
    coroutineScope {
//        val durationMillis = amountOfTime * 60 * 1000L
        val startTime = System.currentTimeMillis()

        val resultTemp = ConcurrentLinkedQueue<ResponseData>()

        val jobs = urls.map { url ->
            launch(Dispatchers.IO) {
                while (System.currentTimeMillis() - startTime < durationMillis) {
                    val responseData = measureResponseTime(url)
//                    println(url) // Для отладки

                    resultTemp.add(responseData)
                }
            }
        }

        delay(durationMillis)
        jobs.forEach { it.cancelAndJoin() }

        return@coroutineScope resultTemp.toList()
    }

suspend fun runR(testCase: TestCase): List<TestCaseResult> {

    val result = when (testCase.runType) {
        RunType.DURATION -> runByDuration(testCase.durationValue ?: 0, testCase.urls)
        RunType.COUNT -> runByCount(testCase.countValue, testCase.urls, testCase.testCaseState.getExecutionCounter())
    }

    return TestCaseResult.create(result)
}

data class ResponseData(val url: String, val statusCode: Int, val duration: Long)
