package com.github.mikeandv.pingwatch.processor

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.entity.ExecutionMode
import com.github.mikeandv.pingwatch.entity.ResponseData
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseParams
import io.mockk.*
import kotlinx.coroutines.test.runTest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*


class RunByCountTest {

    @BeforeTest
    fun setUp() {
        mockkStatic(::measureResponseTimeV2)
    }

    @AfterTest
    fun tearDown() {
        unmockkStatic(::measureResponseTimeV2)
        clearAllMocks()
    }

    @Test
    fun `SEQUENTIAL - returns all results`() = runTest {
        // given
        val urls = linkedMapOf(
            "https://a" to TestCaseParams(
                countValue = 2,
                durationValue = 0,
                isEdit = false,
                unformattedDurationValue = ""
            ),
            "https://b" to TestCaseParams(
                countValue = 3,
                durationValue = 0,
                isEdit = false,
                unformattedDurationValue = ""
            )
        )

        val testCase = TestCase(
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL,
            urls = urls,
            okHttpClient = mockk(relaxed = true),
            parallelism = 8
        )

        coEvery { measureResponseTimeV2(any(), any(), any()) } answers {
            val url = secondArg<String>()
            ResponseData(url = url, statusCode = 200, duration = 10L, errorMessage = "")
        }

        val cancelFlag = { false }

        val result = runByCount(testCase, cancelFlag)

        assertEquals(5, result.size)

        assertEquals(listOf("https://a", "https://a", "https://b", "https://b", "https://b"), result.map { it.url })

        coVerify(exactly = 5) { measureResponseTimeV2(any(), any(), any()) }
    }

    @Test
    fun `SEQUENTIAL - cancel stops early and returns partial results`() = runTest {
        // given
        val urls = linkedMapOf(
            "https://a" to TestCaseParams(
                countValue = 5,
                durationValue = 0,
                isEdit = false,
                unformattedDurationValue = ""
            )
        )

        val testCase = TestCase(
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL,
            urls = urls,
            okHttpClient = mockk(relaxed = true),
            parallelism = 8
        )

        coEvery { measureResponseTimeV2(any(), any(), any()) } answers {
            val url = secondArg<String>()
            ResponseData(url = url, statusCode = 200, duration = 10L, errorMessage = "")
        }

        val calls = AtomicInteger(0)
        val cancelFlag = { calls.get() >= 2 }

        coEvery { measureResponseTimeV2(any(), any(), any()) } answers {
            calls.incrementAndGet()
            val url = secondArg<String>()
            ResponseData(url, 200, 10L, "")
        }

        val result = runByCount(testCase, cancelFlag)

        assertEquals(2, result.size)
        coVerify(exactly = 2) { measureResponseTimeV2(any(), any(), any()) }
    }

    @Test
    fun `PARALLEL - returns all results`() = runTest {
        val urls = linkedMapOf(
            "https://a" to TestCaseParams(
                countValue = 3,
                durationValue = 0,
                isEdit = false,
                unformattedDurationValue = ""
            ),
            "https://b" to TestCaseParams(
                countValue = 2,
                durationValue = 0,
                isEdit = false,
                unformattedDurationValue = ""
            )
        )

        val testCase = TestCase(
            runType = RunType.COUNT,
            executionMode = ExecutionMode.PARALLEL,
            urls = urls,
            okHttpClient = mockk(relaxed = true),
            parallelism = 2
        )

        coEvery { measureResponseTimeV2(any(), any(), any()) } answers {
            val url = secondArg<String>()
            ResponseData(url, 200, 10L, "")
        }

        val cancelFlag = { false }

        val result = runByCount(testCase, cancelFlag)

        assertEquals(5, result.size)

        val grouped = result.groupingBy { it.url }.eachCount()
        assertEquals(3, grouped["https://a"])
        assertEquals(2, grouped["https://b"])

        coVerify(exactly = 5) { measureResponseTimeV2(any(), any(), any()) }
    }

    @Test
    fun `PARALLEL - cancel immediately returns empty and does not call measure`() = runTest {
        val urls = linkedMapOf(
            "https://a" to TestCaseParams(
                countValue = 100,
                durationValue = 0,
                isEdit = false,
                unformattedDurationValue = ""
            )
        )

        val testCase = TestCase(
            runType = RunType.COUNT,
            executionMode = ExecutionMode.PARALLEL,
            urls = urls,
            okHttpClient = mockk(relaxed = true),
            parallelism = 4
        )

        coEvery { measureResponseTimeV2(any(), any(), any()) } returns ResponseData("https://a", 200, 10L, "")

        val cancelFlag = { true }

        val result = runByCount(testCase, cancelFlag)

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { measureResponseTimeV2(any(), any(), any()) }
    }

    @Test
    fun `PARALLEL - cancel mid way stops producing new work (returns partial results)`() = runTest {
        val urls = linkedMapOf(
            "https://a" to TestCaseParams(
                countValue = 100,
                durationValue = 0,
                isEdit = false,
                unformattedDurationValue = ""
            )
        )

        val testCase = TestCase(
            runType = RunType.COUNT,
            executionMode = ExecutionMode.PARALLEL,
            urls = urls,
            okHttpClient = mockk(relaxed = true),
            parallelism = 4
        )

        val completed = AtomicInteger(0)
        val cancelled = AtomicBoolean(false)

        val cancelFlag = { cancelled.get() }

        coEvery { measureResponseTimeV2(any(), any(), any()) } answers {
            val c = completed.incrementAndGet()
            if (c >= 10) cancelled.set(true)
            val url = secondArg<String>()
            ResponseData(url, 200, 10L, "")
        }

        val result = runByCount(testCase, cancelFlag)

        assertTrue(result.size >= 10)
        assertTrue(result.size <= 100)

        assertTrue(completed.get() < 100)
    }
}
