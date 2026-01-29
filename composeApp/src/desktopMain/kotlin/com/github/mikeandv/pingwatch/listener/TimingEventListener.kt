package com.github.mikeandv.pingwatch.listener

import com.github.mikeandv.pingwatch.domain.ErrorType
import com.github.mikeandv.pingwatch.result.RequestTimings
import okhttp3.*
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

class TimingEventListener(private val onFinished: (RequestTimings) -> Unit) : EventListener() {

    private var url: String = ""
    private var statusCode: Int? = null
    private var error: String? = null
    private var errorType: ErrorType = ErrorType.NONE
    private var success: Boolean = false

    private var callStartNs: Long = 0
    private var callEndNs: Long = 0

    private var dnsS: Long? = null
    private var dnsE: Long? = null
    private var connS: Long? = null
    private var connE: Long? = null
    private var tlsS: Long? = null
    private var tlsE: Long? = null
    private var reqHS: Long? = null
    private var reqHE: Long? = null
    private var reqBS: Long? = null
    private var reqBE: Long? = null
    private var respHS: Long? = null
    private var respHE: Long? = null
    private var respBS: Long? = null
    private var respBE: Long? = null

    override fun callStart(call: Call) {
        url = call.request().url.toString()
        callStartNs = System.nanoTime()
    }

    override fun dnsStart(call: Call, domainName: String) {
        dnsS = System.nanoTime()
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        dnsE = System.nanoTime()
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        connS = System.nanoTime()
    }

    override fun secureConnectStart(call: Call) {
        tlsS = System.nanoTime()
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        tlsE = System.nanoTime()
    }

    override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) {
        connE = System.nanoTime()
    }

    override fun requestHeadersStart(call: Call) {
        reqHS = System.nanoTime()
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        reqHE = System.nanoTime()
    }

    override fun requestBodyStart(call: Call) {
        reqBS = System.nanoTime()
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        reqBE = System.nanoTime()
    }

    override fun responseHeadersStart(call: Call) {
        respHS = System.nanoTime()
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        respHE = System.nanoTime()
        statusCode = response.code
    }

    override fun responseBodyStart(call: Call) {
        respBS = System.nanoTime()
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        respBE = System.nanoTime()
    }

    override fun callEnd(call: Call) {
        callEndNs = System.nanoTime()
        // Classify based on HTTP status code
        statusCode?.let { code ->
            errorType = ErrorType.fromStatusCode(code)
            success = errorType == ErrorType.NONE
            if (!success) {
                error = "HTTP $code"
            }
        } ?: run {
            success = true
        }
        publish()
    }

    override fun callFailed(call: Call, ioe: IOException) {
        success = false
        error = ioe.message ?: ioe.javaClass.simpleName
        errorType = ErrorType.fromException(ioe)
        callEndNs = System.nanoTime()
        publish()
    }

    private fun publish() {
        onFinished(
            RequestTimings(
                url = url,
                success = success,
                statusCode = statusCode,
                error = error,
                errorType = errorType,
                callMs = ms(callEndNs - callStartNs),
                dnsMs = dur(dnsS, dnsE),
                connectMs = dur(connS, connE),
                tlsMs = dur(tlsS, tlsE),
                requestHeadersMs = dur(reqHS, reqHE),
                requestBodyMs = dur(reqBS, reqBE),
                responseHeadersMs = dur(respHS, respHE),
                responseBodyMs = dur(respBS, respBE),
            )
        )
    }

    private fun dur(s: Long?, e: Long?): Double? = if (s != null && e != null) ms(e - s) else null
    private fun ms(ns: Long): Double = ns.nanoseconds.toDouble(DurationUnit.MILLISECONDS)
}


