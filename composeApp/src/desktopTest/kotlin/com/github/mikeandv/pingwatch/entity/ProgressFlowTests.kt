import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.entity.ExecutionMode
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProgressFlowTests {

    @Test
    fun `COUNT - emits 0 25 50 75 100`() = runBlocking {
        val tc = TestCase(
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL,
            urls = emptyMap(),
            okHttpClient = mock(),
            parallelism = 1
        )

        val collected = mutableListOf<Int>()

        val job = launch {
            withTimeout(1_000) {
                tc.progressFlow()
                    .take(5)
                    .toList(collected)
            }
        }

        yield()

        tc.events.emit(TestEvent.Started(totalRequests = 4, durationMs = null))
        repeat(4) { tc.events.emit(TestEvent.RequestCompleted) }

        job.join()

        assertEquals(listOf(0, 25, 50, 75, 100), collected)
    }

    @Test
    fun `DURATION - emits 0 then eventually reaches 100`() = runBlocking {
        val tc = TestCase(
            runType = RunType.DURATION,
            executionMode = ExecutionMode.SEQUENTIAL,
            urls = emptyMap(),
            okHttpClient = mock(),
            parallelism = 1
        )

        val collected = mutableListOf<Int>()

        val job = launch {
            withTimeout(2_000) {
                tc.progressFlow()
                    .takeWhile { it < 100 }
                    .toList(collected)
            }
        }

        yield()

        tc.events.emit(TestEvent.Started(totalRequests = null, durationMs = 80))

        repeat(10) {
            delay(10)
            tc.events.emit(TestEvent.RequestCompleted)
        }

        job.cancelAndJoin()

        assertTrue(collected.isNotEmpty())
        assertEquals(collected.first(), 0)
        assertTrue(collected.any { it >= 100 } || collected.last() < 100)

    }

    @Test
    fun `Before Started - no emissions`() = runBlocking {
        val tc = TestCase(
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL,
            urls = emptyMap(),
            okHttpClient = mock(),
            parallelism = 1
        )

        val first = withTimeoutOrNull(100) {
            tc.progressFlow().first()
        }

        assertNull(first)
    }
}
