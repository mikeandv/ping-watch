package com.github.mikeandv.pingwatch

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

val client = OkHttpClient()

fun measureResponseTime(url: String): Long {
    return measureTimeMillis {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            response.body?.string()
        }
    }
}

fun calculatePercentile(sortedList: List<Long>, percentile: Double): Long {
    val index = (percentile / 100.0 * sortedList.size).toInt().coerceAtMost(sortedList.size - 1)
    return sortedList[index]
}

suspend fun runByCount(count: Int, urls: List<String>): List<Pair<String, List<Long>>> = coroutineScope {
    return@coroutineScope urls.map { url ->
        async { url to List(count) { measureResponseTime(url) } }
    }.awaitAll()
}

suspend fun runByDuration(amountOfTime: Int, urls: List<String>): List<Pair<String, List<Long>>> = coroutineScope {
    val durationMillis = amountOfTime * 60 * 1000L
    val startTime = System.currentTimeMillis()

    val resultTemp = ConcurrentHashMap<String, CopyOnWriteArrayList<Long>>()

    val jobs = urls.map { url ->
        launch(Dispatchers.IO) { // Используем IO-диспетчер для сетевых операций
            while (System.currentTimeMillis() - startTime < durationMillis) {
                val responseTime = measureResponseTime(url)
                println(url) // Для отладки

                resultTemp.computeIfAbsent(url) { CopyOnWriteArrayList() }.add(responseTime)
            }
        }
    }

    delay(durationMillis) // Ждем указанное время
    jobs.forEach { it.cancelAndJoin() } // Останавливаем корутины после таймера

    return@coroutineScope resultTemp.map { it.key to it.value.toList() }
}

suspend fun runDurationTest(listUrl: List<String>, duration: Int): String {
    return runR(listUrl, duration, 0, true)
}

suspend fun runCountTest(listUrl: List<String>, count: Int): String {
    return runR(listUrl, 0, count, true)
}

suspend fun runR(listUrl: List<String>, duration: Int, count: Int, isDuration: Boolean): String {
//    val urls = listOf(
//        "https://example.com",
//        "https://google.com",
//        "https://github.com"
//    )


    val results = if (isDuration) {
        runByDuration(duration, listUrl)
    } else {
        runByCount(count, listUrl)
    }


    val resultString = buildString {
        for (item in results) {
            val sortedTimes = item.second.sorted()

            val min = sortedTimes.first()
            val max = sortedTimes.last()
            val avg = sortedTimes.average()
            val median = calculatePercentile(sortedTimes, 50.0)
            val p95 = calculatePercentile(sortedTimes, 95.0)
            val p99 = calculatePercentile(sortedTimes, 99.0)


            append("Url: ${item.first}\n")
            append("Requests count: ${sortedTimes.size}\n")
            append("Min: ${min}ms, Max: ${max}ms, Avg: ${"%.2f".format(avg)}ms\n")
            append("Median: ${median}ms, P95: ${p95}ms, P99: ${p99}ms\n")
            append("\n")
        }
    }


    return resultString
}
