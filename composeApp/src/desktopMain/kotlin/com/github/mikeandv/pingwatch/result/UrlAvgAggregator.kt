package com.github.mikeandv.pingwatch.result

import java.util.concurrent.ConcurrentHashMap

class UrlAvgAggregator {

    private data class SumCnt(var sum: Long = 0, var cnt: Int = 0) {
        fun add(v: Long) {
            sum += v; cnt++
        }

        fun avgOrNull(): Double? = if (cnt == 0) null else sum.toDouble() / cnt
    }

    private data class Bucket(
        val url: String,
        var count: Int = 0,
        var ok: Int = 0,
        var fail: Int = 0,
        val call: SumCnt = SumCnt(),
        val dns: SumCnt = SumCnt(),
        val connect: SumCnt = SumCnt(),
        val tls: SumCnt = SumCnt(),
        val reqH: SumCnt = SumCnt(),
        val reqB: SumCnt = SumCnt(),
        val respH: SumCnt = SumCnt(),
        val respB: SumCnt = SumCnt()
    )

    private val map = ConcurrentHashMap<String, Bucket>()

    fun add(t: RequestTimings) {
        val b = map.computeIfAbsent(t.url) { Bucket(url = it) }
        synchronized(b) {
            b.count++
            if (t.success) b.ok++ else b.fail++

            b.call.add(t.callMs)
            t.dnsMs?.let { b.dns.add(it) }
            t.connectMs?.let { b.connect.add(it) }
            t.tlsMs?.let { b.tls.add(it) }
            t.requestHeadersMs?.let { b.reqH.add(it) }
            t.requestBodyMs?.let { b.reqB.add(it) }
            t.responseHeadersMs?.let { b.respH.add(it) }
            t.responseBodyMs?.let { b.respB.add(it) }
        }
    }

    fun snapshot(): Map<String, UrlAvgMetrics> =
        map.mapValues { (_, b) ->
            synchronized(b) {
                UrlAvgMetrics(
                    url = b.url,
                    count = b.count,
                    ok = b.ok,
                    fail = b.fail,

                    callAvgMs = b.call.avgOrNull() ?: 0.0,

                    dnsAvgMs = b.dns.avgOrNull(),
                    connectAvgMs = b.connect.avgOrNull(),
                    tlsAvgMs = b.tls.avgOrNull(),
                    requestHeadersAvgMs = b.reqH.avgOrNull(),
                    requestBodyAvgMs = b.reqB.avgOrNull(),
                    responseHeadersAvgMs = b.respH.avgOrNull(),
                    responseBodyAvgMs = b.respB.avgOrNull()
                )
            }
        }

    fun clear() = map.clear()
}