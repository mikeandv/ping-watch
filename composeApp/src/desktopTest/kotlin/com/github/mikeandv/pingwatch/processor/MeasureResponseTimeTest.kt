package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.entity.TestEvent
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.mockito.kotlin.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MeasureResponseTimeTest {

    @Test
    fun `success returns 200`() = runTest {
        val client = mock<OkHttpClient>()
        val call = mock<Call>()
        whenever(client.newCall(any())).thenReturn(call)

        doAnswer { inv ->
            val cb = inv.arguments[0] as Callback

            val url = "https://example.com/"
            val response = Response.Builder()
                .request(Request.Builder().url(url).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()

            cb.onResponse(call, response)
            null
        }.whenever(call).enqueue(any())

        val events = MutableSharedFlow<TestEvent>()
        val result = measureResponseTimeV2(client, "https://example.com/", events)

        assertEquals(200, result.statusCode)
        verify(call).enqueue(any())
    }

    @Test
    fun `cancel calls okhttp cancel`() = runTest {
        val client = mock<OkHttpClient>()
        val call = mock<Call>()
        whenever(client.newCall(any())).thenReturn(call)

        doAnswer { null }.whenever(call).enqueue(any())

        val events = MutableSharedFlow<TestEvent>()

        val job = launch {
            measureResponseTimeV2(client, "https://example.com/", events)
        }

        yield()

        job.cancelAndJoin()

        verify(call).cancel()
    }
}

