import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.entity.ExecutionMode
import com.github.mikeandv.pingwatch.entity.ResponseData
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseParams
import com.github.mikeandv.pingwatch.processor.measureResponseTimeV2
import com.github.mikeandv.pingwatch.processor.runByDuration
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*

class RunByDurationTest {

    @BeforeTest
    fun setup() {
        mockkStatic(::measureResponseTimeV2)
    }

    @AfterTest
    fun tearDown() {
        unmockkStatic(::measureResponseTimeV2)
        clearAllMocks()
    }

    @Test
    fun `SEQUENTIAL - returns some results`() = runTest {
        val client = mockk<OkHttpClient>(relaxed = true)

        val urls = linkedMapOf(
            "u1" to TestCaseParams(countValue = 0, durationValue = 30L, isEdit = false, unformattedDurationValue = ""),
            "u2" to TestCaseParams(countValue = 0, durationValue = 30L, isEdit = false, unformattedDurationValue = "")
        )

        val testCase = TestCase(
            runType = RunType.DURATION,
            executionMode = ExecutionMode.SEQUENTIAL,
            urls = urls,
            okHttpClient = client,
            parallelism = 2
        )

        coEvery { measureResponseTimeV2(any(), any(), any()) } answers {
            val url = secondArg<String>()
            ResponseData(url, 200, 1L, "")
        }

        val result = runByDuration(testCase, cancelFlag = { false })

        assertTrue(result.isNotEmpty(), "Should return at least 1 result")
        assertTrue(result.all { it.url == "u1" || it.url == "u2" })

        coVerify(atLeast = 1) { measureResponseTimeV2(any(), any(), any()) }
    }

    @Test
    fun `SEQUENTIAL - cancel stops early`() = runTest {
        val client = mockk<OkHttpClient>(relaxed = true)

        val urls = linkedMapOf(
            "u1" to TestCaseParams(
                countValue = 0,
                durationValue = 10_000L,
                isEdit = false,
                unformattedDurationValue = ""
            )
        )

        val testCase = TestCase(
            runType = RunType.DURATION,
            executionMode = ExecutionMode.SEQUENTIAL,
            urls = urls,
            okHttpClient = client,
            parallelism = 2
        )

        val completed = AtomicInteger(0)
        val cancel = AtomicBoolean(false)

        coEvery { measureResponseTimeV2(any(), any(), any()) } answers {
            val c = completed.incrementAndGet()
            if (c >= 3) cancel.set(true)
            ResponseData("u1", 200, 1L, "")
        }

        val result = runByDuration(testCase, cancelFlag = { cancel.get() })

        assertEquals(3, result.size, "should return 3 results after cancel")
        coVerify(exactly = 3) { measureResponseTimeV2(any(), any(), any()) }
    }

    @Test
    fun `PARALLEL - returns some results`() = runTest {
        val client = mockk<OkHttpClient>(relaxed = true)

        val urls = linkedMapOf(
            "u1" to TestCaseParams(countValue = 0, durationValue = 40L, isEdit = false, unformattedDurationValue = ""),
            "u2" to TestCaseParams(countValue = 0, durationValue = 40L, isEdit = false, unformattedDurationValue = "")
        )

        val testCase = TestCase(
            runType = RunType.DURATION,
            executionMode = ExecutionMode.PARALLEL,
            urls = urls,
            okHttpClient = client,
            parallelism = 2
        )

        coEvery { measureResponseTimeV2(any(), any(), any()) } answers {
            val url = secondArg<String>()
            ResponseData(url, 200, 1L, "")
        }

        val result = runByDuration(testCase, cancelFlag = { false })

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.url == "u1" || it.url == "u2" })

        coVerify(atLeast = 1) { measureResponseTimeV2(any(), any(), any()) }
    }

    @Test
    fun `PARALLEL - cancel immediately returns empty`() = runTest {
        val client = mockk<OkHttpClient>(relaxed = true)

        val urls = linkedMapOf(
            "u1" to TestCaseParams(countValue = 0, durationValue = 200L, isEdit = false, unformattedDurationValue = ""),
            "u2" to TestCaseParams(countValue = 0, durationValue = 200L, isEdit = false, unformattedDurationValue = "")
        )

        val testCase = TestCase(
            runType = RunType.DURATION,
            executionMode = ExecutionMode.PARALLEL,
            urls = urls,
            okHttpClient = client,
            parallelism = 4
        )

        coEvery { measureResponseTimeV2(any(), any(), any()) } answers {
            val url = secondArg<String>()
            ResponseData(url, 200, 1L, "")
        }

        val result = runByDuration(testCase, cancelFlag = { true })

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { measureResponseTimeV2(any(), any(), any()) }
    }

    @Test
    fun `PARALLEL - cancel mid way stops eventually (non-strict)`() = runTest {
        val client = mockk<OkHttpClient>(relaxed = true)

        val urls = linkedMapOf(
            "u1" to TestCaseParams(
                countValue = 0,
                durationValue = 10_000L,
                isEdit = false,
                unformattedDurationValue = ""
            ),
            "u2" to TestCaseParams(
                countValue = 0,
                durationValue = 10_000L,
                isEdit = false,
                unformattedDurationValue = ""
            )
        )

        val testCase = TestCase(
            runType = RunType.DURATION,
            executionMode = ExecutionMode.PARALLEL,
            urls = urls,
            okHttpClient = client,
            parallelism = 4
        )

        val completed = AtomicInteger(0)
        val cancel = AtomicBoolean(false)

        coEvery { measureResponseTimeV2(any(), any(), any()) } answers {
            val c = completed.incrementAndGet()
            if (c >= 20) cancel.set(true)
            val url = secondArg<String>()
            ResponseData(url, 200, 1L, "")
        }

        val result = runByDuration(testCase, cancelFlag = { cancel.get() })

        assertTrue(result.size >= 20)
        assertTrue(result.size < 200)
    }
}
