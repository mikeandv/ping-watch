package com.github.mikeandv.pingwatch.ui.handlers

import com.github.mikeandv.pingwatch.domain.*
import kotlin.test.*

class ValidateLaunchTestTest {

    private fun createTestCase(
        urls: Map<String, TestCaseParams> = emptyMap(),
        runType: RunType = RunType.COUNT,
        executionMode: ExecutionMode = ExecutionMode.SEQUENTIAL,
        parallelism: Int = 4
    ): TestCase {
        val settings = TestCaseSettings()
        return TestCase(
            urls = urls,
            runType = runType,
            executionMode = executionMode,
            parallelism = parallelism,
            settings = settings
        )
    }

    private fun createParams(
        countValue: Long = 10,
        durationValue: Long = 1000
    ) = TestCaseParams(
        isEdit = false,
        countValue = countValue,
        durationValue = durationValue,
        unformattedDurationValue = "01:00",
        tag = null
    )

    // Valid cases

    @Test
    fun `valid COUNT SEQUENTIAL test should return valid`() {
        val testCase = createTestCase(
            urls = mapOf("https://example.com" to createParams(countValue = 10)),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        val result = validateLaunchTest(testCase, null, null)

        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun `valid DURATION SEQUENTIAL test should return valid`() {
        val testCase = createTestCase(
            urls = mapOf("https://example.com" to createParams(durationValue = 60000)),
            runType = RunType.DURATION,
            executionMode = ExecutionMode.SEQUENTIAL
        )

        val result = validateLaunchTest(testCase, null, null)

        assertTrue(result.isValid)
    }

    @Test
    fun `valid COUNT PARALLEL test should return valid`() {
        val testCase = createTestCase(
            urls = mapOf("https://example.com" to createParams(countValue = 10)),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.PARALLEL,
            parallelism = 4
        )

        val result = validateLaunchTest(testCase, null, null)

        assertTrue(result.isValid)
    }

    @Test
    fun `valid DURATION PARALLEL test should return valid`() {
        val testCase = createTestCase(
            urls = mapOf("https://example.com" to createParams(durationValue = 60000)),
            runType = RunType.DURATION,
            executionMode = ExecutionMode.PARALLEL,
            parallelism = 4
        )

        val result = validateLaunchTest(testCase, null, null)

        assertTrue(result.isValid)
    }

    // Empty URLs

    @Test
    fun `empty URLs should return invalid`() {
        val testCase = createTestCase(urls = emptyMap())

        val result = validateLaunchTest(testCase, null, null)

        assertFalse(result.isValid)
        assertEquals(result.errorMessage?.contains("URLs list is empty"), true)
    }

    // Duration errors

    @Test
    fun `duration error message should return invalid`() {
        val testCase = createTestCase(
            urls = mapOf("https://example.com" to createParams())
        )

        val result = validateLaunchTest(testCase, "Invalid duration", null)

        assertFalse(result.isValid)
        assertEquals(result.errorMessage?.contains("Duration format is incorrect"), true)
    }

    // Parallelism errors

    @Test
    fun `parallelism error in PARALLEL mode should return invalid`() {
        val testCase = createTestCase(
            urls = mapOf("https://example.com" to createParams()),
            executionMode = ExecutionMode.PARALLEL,
            parallelism = 4
        )

        val result = validateLaunchTest(testCase, null, "Invalid parallelism")

        assertFalse(result.isValid)
        assertEquals(result.errorMessage?.contains("Parallelism value is incorrect"), true)
    }

    @Test
    fun `parallelism error in SEQUENTIAL mode should be ignored`() {
        val testCase = createTestCase(
            urls = mapOf("https://example.com" to createParams()),
            executionMode = ExecutionMode.SEQUENTIAL
        )

        val result = validateLaunchTest(testCase, null, "Invalid parallelism")

        assertTrue(result.isValid)
    }

    @Test
    fun `zero parallelism in PARALLEL mode should return invalid`() {
        val testCase = createTestCase(
            urls = mapOf("https://example.com" to createParams()),
            executionMode = ExecutionMode.PARALLEL,
            parallelism = 0
        )

        val result = validateLaunchTest(testCase, null, null)

        assertFalse(result.isValid)
        assertEquals(result.errorMessage?.contains("Parallelism doesn't set"), true)
    }

    @Test
    fun `zero parallelism in SEQUENTIAL mode should be valid`() {
        val testCase = createTestCase(
            urls = mapOf("https://example.com" to createParams()),
            executionMode = ExecutionMode.SEQUENTIAL,
            parallelism = 0
        )

        val result = validateLaunchTest(testCase, null, null)

        assertTrue(result.isValid)
    }

    // Duration mode validation

    @Test
    fun `DURATION mode with zero duration should return invalid`() {
        val testCase = createTestCase(
            urls = mapOf("https://example.com" to createParams(durationValue = 0)),
            runType = RunType.DURATION
        )

        val result = validateLaunchTest(testCase, null, null)

        assertFalse(result.isValid)
        assertEquals(result.errorMessage?.contains("Time duration doesn't set"), true)
    }

    @Test
    fun `DURATION mode with some zero durations should list affected URLs`() {
        val testCase = createTestCase(
            urls = mapOf(
                "https://example1.com" to createParams(durationValue = 60000),
                "https://example2.com" to createParams(durationValue = 0)
            ),
            runType = RunType.DURATION
        )

        val result = validateLaunchTest(testCase, null, null)

        assertFalse(result.isValid)
        assertEquals(result.errorMessage?.contains("https://example2.com"), true)
    }

    // Count mode validation

    @Test
    fun `COUNT mode with zero count should return invalid`() {
        val testCase = createTestCase(
            urls = mapOf("https://example.com" to createParams(countValue = 0)),
            runType = RunType.COUNT
        )

        val result = validateLaunchTest(testCase, null, null)

        assertFalse(result.isValid)
        assertEquals(result.errorMessage?.contains("Requests count doesn't set"), true)
    }

    @Test
    fun `COUNT mode with some zero counts should list affected URLs`() {
        val testCase = createTestCase(
            urls = mapOf(
                "https://example1.com" to createParams(countValue = 10),
                "https://example2.com" to createParams(countValue = 0)
            ),
            runType = RunType.COUNT
        )

        val result = validateLaunchTest(testCase, null, null)

        assertFalse(result.isValid)
        assertEquals(result.errorMessage?.contains("https://example2.com"), true)
    }

    // Multiple errors

    @Test
    fun `multiple errors should all be reported`() {
        val testCase = createTestCase(
            urls = emptyMap(),
            executionMode = ExecutionMode.PARALLEL,
            parallelism = 0
        )

        val result = validateLaunchTest(testCase, "Duration error", "Parallelism error")

        assertFalse(result.isValid)
        assertEquals(result.errorMessage?.contains("URLs list is empty"), true)
        assertEquals(result.errorMessage?.contains("Duration format is incorrect"), true)
        assertEquals(result.errorMessage?.contains("Parallelism value is incorrect"), true)
        assertEquals(result.errorMessage?.contains("Parallelism doesn't set"), true)
    }
}
