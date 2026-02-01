package com.github.mikeandv.pingwatch.listener

import com.github.mikeandv.pingwatch.domain.ErrorType
import com.github.mikeandv.pingwatch.result.RequestTimings
import io.mockk.every
import io.mockk.mockk
import okhttp3.*
import java.io.IOException
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NoRouteToHostException
import java.net.Proxy
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlin.test.*

class TimingEventListenerTest {

    private lateinit var capturedTimings: RequestTimings
    private lateinit var listener: TimingEventListener

    @BeforeTest
    fun setUp() {
        listener = TimingEventListener { timings ->
            capturedTimings = timings
        }
    }

    private fun createMockCall(url: String = "https://example.com/test"): Call {
        val request = mockk<Request>()
        val httpUrl = mockk<HttpUrl>()
        every { httpUrl.toString() } returns url
        every { request.url } returns httpUrl

        val call = mockk<Call>()
        every { call.request() } returns request
        return call
    }

    private fun createMockResponse(statusCode: Int): Response {
        val response = mockk<Response>()
        every { response.code } returns statusCode
        return response
    }

    // callStart tests

    @Test
    fun `callStart should capture URL from request`() {
        val url = "https://test.example.com/api/endpoint"
        val call = createMockCall(url)

        listener.callStart(call)
        listener.callEnd(call)

        assertEquals(url, capturedTimings.url)
    }

    // callEnd tests

    @Test
    fun `callEnd should mark success for 200 status code`() {
        val call = createMockCall()
        val response = createMockResponse(200)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertTrue(capturedTimings.success)
        assertEquals(200, capturedTimings.statusCode)
        assertEquals(ErrorType.NONE, capturedTimings.errorType)
        assertNull(capturedTimings.error)
    }

    @Test
    fun `callEnd should mark success for 201 status code`() {
        val call = createMockCall()
        val response = createMockResponse(201)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertTrue(capturedTimings.success)
        assertEquals(201, capturedTimings.statusCode)
        assertEquals(ErrorType.NONE, capturedTimings.errorType)
    }

    @Test
    fun `callEnd should mark success for 304 status code`() {
        val call = createMockCall()
        val response = createMockResponse(304)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertTrue(capturedTimings.success)
        assertEquals(304, capturedTimings.statusCode)
        assertEquals(ErrorType.NONE, capturedTimings.errorType)
    }

    @Test
    fun `callEnd should mark error for 400 status code`() {
        val call = createMockCall()
        val response = createMockResponse(400)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertFalse(capturedTimings.success)
        assertEquals(400, capturedTimings.statusCode)
        assertEquals(ErrorType.HTTP_CRITICAL_ERROR, capturedTimings.errorType)
        assertEquals("HTTP 400", capturedTimings.error)
    }

    @Test
    fun `callEnd should mark error for 404 status code`() {
        val call = createMockCall()
        val response = createMockResponse(404)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertFalse(capturedTimings.success)
        assertEquals(404, capturedTimings.statusCode)
        assertEquals(ErrorType.HTTP_CRITICAL_ERROR, capturedTimings.errorType)
        assertEquals("HTTP 404", capturedTimings.error)
    }

    @Test
    fun `callEnd should mark error for 429 status code as client error`() {
        val call = createMockCall()
        val response = createMockResponse(429)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertFalse(capturedTimings.success)
        assertEquals(429, capturedTimings.statusCode)
        assertEquals(ErrorType.HTTP_CLIENT_ERROR, capturedTimings.errorType)
    }

    @Test
    fun `callEnd should mark error for 500 status code`() {
        val call = createMockCall()
        val response = createMockResponse(500)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertFalse(capturedTimings.success)
        assertEquals(500, capturedTimings.statusCode)
        assertEquals(ErrorType.HTTP_SERVER_ERROR, capturedTimings.errorType)
        assertEquals("HTTP 500", capturedTimings.error)
    }

    @Test
    fun `callEnd should mark error for 503 status code`() {
        val call = createMockCall()
        val response = createMockResponse(503)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertFalse(capturedTimings.success)
        assertEquals(503, capturedTimings.statusCode)
        assertEquals(ErrorType.HTTP_SERVER_ERROR, capturedTimings.errorType)
    }

    @Test
    fun `callEnd should mark success when no status code captured`() {
        val call = createMockCall()

        listener.callStart(call)
        listener.callEnd(call)

        assertTrue(capturedTimings.success)
        assertNull(capturedTimings.statusCode)
        assertEquals(ErrorType.NONE, capturedTimings.errorType)
    }

    // callFailed tests

    @Test
    fun `callFailed should classify SocketTimeoutException as TIMEOUT`() {
        val call = createMockCall()
        val exception = SocketTimeoutException("Connection timed out")

        listener.callStart(call)
        listener.callFailed(call, exception)

        assertFalse(capturedTimings.success)
        assertEquals(ErrorType.TIMEOUT, capturedTimings.errorType)
        assertEquals("Connection timed out", capturedTimings.error)
    }

    @Test
    fun `callFailed should classify ConnectException as CONNECTION_REFUSED`() {
        val call = createMockCall()
        val exception = ConnectException("Connection refused")

        listener.callStart(call)
        listener.callFailed(call, exception)

        assertFalse(capturedTimings.success)
        assertEquals(ErrorType.CONNECTION_REFUSED, capturedTimings.errorType)
        assertEquals("Connection refused", capturedTimings.error)
    }

    @Test
    fun `callFailed should classify UnknownHostException as DNS_FAILURE`() {
        val call = createMockCall()
        val exception = UnknownHostException("Unknown host")

        listener.callStart(call)
        listener.callFailed(call, exception)

        assertFalse(capturedTimings.success)
        assertEquals(ErrorType.DNS_FAILURE, capturedTimings.errorType)
        assertEquals("Unknown host", capturedTimings.error)
    }

    @Test
    fun `callFailed should classify NoRouteToHostException as HOST_UNREACHABLE`() {
        val call = createMockCall()
        val exception = NoRouteToHostException("No route to host")

        listener.callStart(call)
        listener.callFailed(call, exception)

        assertFalse(capturedTimings.success)
        assertEquals(ErrorType.HOST_UNREACHABLE, capturedTimings.errorType)
        assertEquals("No route to host", capturedTimings.error)
    }

    @Test
    fun `callFailed should classify SSLException as SSL_ERROR`() {
        val call = createMockCall()
        val exception = SSLException("SSL handshake failed")

        listener.callStart(call)
        listener.callFailed(call, exception)

        assertFalse(capturedTimings.success)
        assertEquals(ErrorType.SSL_ERROR, capturedTimings.errorType)
        assertEquals("SSL handshake failed", capturedTimings.error)
    }

    @Test
    fun `callFailed should classify generic IOException as NETWORK_ERROR`() {
        val call = createMockCall()
        val exception = IOException("Network error")

        listener.callStart(call)
        listener.callFailed(call, exception)

        assertFalse(capturedTimings.success)
        assertEquals(ErrorType.NETWORK_ERROR, capturedTimings.errorType)
        assertEquals("Network error", capturedTimings.error)
    }

    @Test
    fun `callFailed should use class name when exception message is null`() {
        val call = createMockCall()
        val exception = IOException()

        listener.callStart(call)
        listener.callFailed(call, exception)

        assertFalse(capturedTimings.success)
        assertEquals("IOException", capturedTimings.error)
    }

    // Timing calculation tests

    @Test
    fun `callMs should be positive after callEnd`() {
        val call = createMockCall()
        val response = createMockResponse(200)

        listener.callStart(call)
        Thread.sleep(10)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertTrue(capturedTimings.callMs > 0)
    }

    @Test
    fun `dnsMs should be calculated when dns events occur`() {
        val call = createMockCall()
        val response = createMockResponse(200)

        listener.callStart(call)
        listener.dnsStart(call, "example.com")
        Thread.sleep(5)
        listener.dnsEnd(call, "example.com", listOf(InetAddress.getLoopbackAddress()))
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertNotNull(capturedTimings.dnsMs)
        assertTrue(capturedTimings.dnsMs!! >= 0)
    }

    @Test
    fun `dnsMs should be null when dns events do not occur`() {
        val call = createMockCall()
        val response = createMockResponse(200)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertNull(capturedTimings.dnsMs)
    }

    @Test
    fun `connectMs should be calculated when connect events occur`() {
        val call = createMockCall()
        val response = createMockResponse(200)
        val socketAddress = InetSocketAddress.createUnresolved("example.com", 443)

        listener.callStart(call)
        listener.connectStart(call, socketAddress, Proxy.NO_PROXY)
        Thread.sleep(5)
        listener.connectEnd(call, socketAddress, Proxy.NO_PROXY, Protocol.HTTP_1_1)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertNotNull(capturedTimings.connectMs)
        assertTrue(capturedTimings.connectMs!! >= 0)
    }

    @Test
    fun `connectMs should be null when connect events do not occur`() {
        val call = createMockCall()
        val response = createMockResponse(200)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertNull(capturedTimings.connectMs)
    }

    @Test
    fun `tlsMs should be calculated when secure connect events occur`() {
        val call = createMockCall()
        val response = createMockResponse(200)

        listener.callStart(call)
        listener.secureConnectStart(call)
        Thread.sleep(5)
        listener.secureConnectEnd(call, null)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertNotNull(capturedTimings.tlsMs)
        assertTrue(capturedTimings.tlsMs!! >= 0)
    }

    @Test
    fun `tlsMs should be null when secure connect events do not occur`() {
        val call = createMockCall()
        val response = createMockResponse(200)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertNull(capturedTimings.tlsMs)
    }

    @Test
    fun `requestHeadersMs should be calculated when request header events occur`() {
        val call = createMockCall()
        val response = createMockResponse(200)
        val request = mockk<Request>()

        listener.callStart(call)
        listener.requestHeadersStart(call)
        Thread.sleep(5)
        listener.requestHeadersEnd(call, request)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertNotNull(capturedTimings.requestHeadersMs)
        assertTrue(capturedTimings.requestHeadersMs!! >= 0)
    }

    @Test
    fun `requestBodyMs should be calculated when request body events occur`() {
        val call = createMockCall()
        val response = createMockResponse(200)

        listener.callStart(call)
        listener.requestBodyStart(call)
        Thread.sleep(5)
        listener.requestBodyEnd(call, 100)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertNotNull(capturedTimings.requestBodyMs)
        assertTrue(capturedTimings.requestBodyMs!! >= 0)
    }

    @Test
    fun `responseHeadersMs should be calculated when response header events occur`() {
        val call = createMockCall()
        val response = createMockResponse(200)

        listener.callStart(call)
        listener.responseHeadersStart(call)
        Thread.sleep(5)
        listener.responseHeadersEnd(call, response)
        listener.callEnd(call)

        assertNotNull(capturedTimings.responseHeadersMs)
        assertTrue(capturedTimings.responseHeadersMs!! >= 0)
    }

    @Test
    fun `responseBodyMs should be calculated when response body events occur`() {
        val call = createMockCall()
        val response = createMockResponse(200)

        listener.callStart(call)
        listener.responseHeadersEnd(call, response)
        listener.responseBodyStart(call)
        Thread.sleep(5)
        listener.responseBodyEnd(call, 1000)
        listener.callEnd(call)

        assertNotNull(capturedTimings.responseBodyMs)
        assertTrue(capturedTimings.responseBodyMs!! >= 0)
    }

    // Full request lifecycle test

    @Test
    fun `full request lifecycle should capture all timings`() {
        val call = createMockCall("https://api.example.com/data")
        val response = createMockResponse(200)
        val request = mockk<Request>()
        val socketAddress = InetSocketAddress.createUnresolved("api.example.com", 443)

        listener.callStart(call)
        listener.dnsStart(call, "api.example.com")
        listener.dnsEnd(call, "api.example.com", listOf(InetAddress.getLoopbackAddress()))
        listener.connectStart(call, socketAddress, Proxy.NO_PROXY)
        listener.secureConnectStart(call)
        listener.secureConnectEnd(call, null)
        listener.connectEnd(call, socketAddress, Proxy.NO_PROXY, Protocol.HTTP_2)
        listener.requestHeadersStart(call)
        listener.requestHeadersEnd(call, request)
        listener.requestBodyStart(call)
        listener.requestBodyEnd(call, 50)
        listener.responseHeadersStart(call)
        listener.responseHeadersEnd(call, response)
        listener.responseBodyStart(call)
        listener.responseBodyEnd(call, 500)
        listener.callEnd(call)

        assertEquals("https://api.example.com/data", capturedTimings.url)
        assertTrue(capturedTimings.success)
        assertEquals(200, capturedTimings.statusCode)
        assertEquals(ErrorType.NONE, capturedTimings.errorType)
        assertNull(capturedTimings.error)
        assertTrue(capturedTimings.callMs >= 0)
        assertNotNull(capturedTimings.dnsMs)
        assertNotNull(capturedTimings.connectMs)
        assertNotNull(capturedTimings.tlsMs)
        assertNotNull(capturedTimings.requestHeadersMs)
        assertNotNull(capturedTimings.requestBodyMs)
        assertNotNull(capturedTimings.responseHeadersMs)
        assertNotNull(capturedTimings.responseBodyMs)
    }
}
