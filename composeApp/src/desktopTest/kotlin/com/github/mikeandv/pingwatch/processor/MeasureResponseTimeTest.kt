package com.github.mikeandv.pingwatch.processor

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import kotlin.test.Test
import okhttp3.OkHttpClient
import okhttp3.Response
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MeasureResponseTimeTest {
    private val client: OkHttpClient = mock()

    @Test
    fun `should return correct response data for valid URL`() = runBlocking {
        val call: Call = mock()
        val callbackCaptor = argumentCaptor<Callback>()
        val response: Response = mock()

        whenever(client.newCall(any())).thenReturn(call)

        doAnswer { invocation ->
            val callback = invocation.arguments[0] as Callback

            runBlocking {
                delay(10)
            }

            callback.onResponse(call, response)
        }.whenever(call).enqueue(callbackCaptor.capture())

        whenever(response.code).thenReturn(200)


        val result = measureResponseTimeV2(client, "http://example.com")

        assertEquals(200, result.statusCode)
        assertTrue { result.duration > 0 }

    }

    @Test
    fun `should handle exceptions gracefully`() = runBlocking {
        val call: Call = mock()
        val callbackCaptor = argumentCaptor<Callback>()

        whenever(client.newCall(any())).thenReturn(call)
        doAnswer { invocation ->
            val callback = invocation.arguments[0] as Callback
            // Ручной вызов метода `onFailure`
            callback.onFailure(call, IOException("Mocked failure"))
            null
        }.whenever(call).enqueue(callbackCaptor.capture())


        val result = measureResponseTimeV2(client, "http://example.com")
        assertEquals(-1, result.statusCode)
        assertEquals(-1L, result.duration)
    }
}

