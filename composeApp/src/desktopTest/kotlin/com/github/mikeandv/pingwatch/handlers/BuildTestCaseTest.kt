package com.github.mikeandv.pingwatch.handlers

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.entity.ExecutionMode
import com.github.mikeandv.pingwatch.entity.TestCase
import okhttp3.OkHttpClient
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildTestCaseTest {
    private val client: OkHttpClient = mock()

    @Test
    fun `buildTestCase should set run type`() {
        val original = TestCase(client, emptyMap(), RunType.COUNT, ExecutionMode.SEQUENTIAL, 8)

        val result = buildTestCase(original, emptyMap(), isDuration = true)

        assertEquals(RunType.DURATION, result.runType)
    }
}