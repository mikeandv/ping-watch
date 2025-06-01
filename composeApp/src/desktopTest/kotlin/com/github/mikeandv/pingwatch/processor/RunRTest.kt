package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseParams
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunRTest {
    private val client: OkHttpClient = mock()

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