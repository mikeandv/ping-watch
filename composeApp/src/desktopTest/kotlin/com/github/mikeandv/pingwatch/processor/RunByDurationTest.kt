package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.entity.TestCaseParams
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test

import kotlin.test.assertTrue

class RunByDurationTest {
    private val client: OkHttpClient = mock()

    @BeforeTest
    fun prepareMock() {
        val call: Call = mock()
        val callbackCaptor = argumentCaptor<Callback>()
        val response: Response = mock()

        whenever(client.newCall(any())).thenReturn(call)

        doAnswer { invocation ->
            val callback = invocation.arguments[0] as Callback
            callback.onResponse(call, response)
        }.whenever(call).enqueue(callbackCaptor.capture())

        whenever(response.code).thenReturn(200)
    }

    @Test
    fun `should execute tasks within the given duration`() = runBlocking {
        val urls = mapOf("http://example.com" to TestCaseParams(false, 0L, 1000L, "00:01"))

        val result = runByDurationV2(client, urls) { false }
        assertTrue { result.isNotEmpty() }
    }

    @Test
    fun `should cancel job execution when canselFlag is true`() = runBlocking {
        val urls = mapOf("http://example.com" to TestCaseParams(false, 0, 2000L, "00:05"))

        val startTime = System.currentTimeMillis()

        val result = runByDurationV2(client, urls) {
            System.currentTimeMillis() - startTime > 1000L
        }

        assertTrue { result.isNotEmpty()}
        assertTrue { result.size < 50000}
    }
}