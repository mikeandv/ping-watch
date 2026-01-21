package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.entity.TestCaseParams
import kotlinx.coroutines.delay
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
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

class RunByCountTest {
    private val client: OkHttpClient = mock()

    @BeforeTest
    fun prepareMock() {
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
    }

    @Test
    fun `should execute tasks the specified number of times`() = runBlocking {
        val urls = mapOf("http://example.com" to TestCaseParams(false, 3, 0L, ""))
        val executionCounter = AtomicLong(0)

        val result = runByCountV2(client, urls, executionCounter) { false }
        assertEquals(3, executionCounter.get())
        assertEquals(3, result.size)

    }

    @Test
    fun `should cancel job execution when canselFlag is true`() = runBlocking {
        val urls = mapOf("http://example.com" to TestCaseParams(false, 5, 0L, ""))
        val executionCounter = AtomicLong(0)
        var cancelCalled: Boolean

        val result = runByCountV2(client, urls, executionCounter) {
            cancelCalled = executionCounter.get() >= 3
            cancelCalled
        }

        assertTrue { executionCounter.get() in 1..3 }
        assertTrue { result.size in 1..3 }

    }
}