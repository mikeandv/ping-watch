package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.entity.TestCase
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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunRTest {
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
    fun `should execute with RunType COUNT`() = runBlocking {
        val testCase = TestCase(
            client,
            runType = RunType.COUNT,
            urls = mapOf("http://example.com" to TestCaseParams(false, 3L, 0L, ""))
        )

        val result = runR(testCase) { false }
        assertEquals(1, result.size)
    }

    @Test
    fun `should execute with RunType DURATION`() = runBlocking {
        val testCase = TestCase(
            client,
            runType = RunType.DURATION,
            urls = mapOf("http://example.com" to TestCaseParams(false, 0, 1000L, "00:01"))
        )

        val result = runR(testCase) { false }
        assertTrue { result.isNotEmpty() }
    }
}