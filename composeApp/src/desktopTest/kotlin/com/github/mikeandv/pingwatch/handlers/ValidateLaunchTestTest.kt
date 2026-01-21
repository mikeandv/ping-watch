package com.github.mikeandv.pingwatch.handlers

import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseParams
import kotlinx.coroutines.test.TestScope
import org.mockito.kotlin.mock
import kotlin.test.*

class ValidateLaunchTestTest {

    @Test
    fun `empty url list should fail`() {
        val result = validateLaunchTest(
            urlList = emptyMap(),
            isDuration = true,
            durationErrorMessage = null
        )

        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("URLs list is empty!"))
    }

    @Test
    fun `not null durationErrorMessage should fail`() {
        val result = validateLaunchTest(
            urlList = mapOf("url" to TestCaseParams(false, 10L, 0L, "")),
            isDuration = false,
            durationErrorMessage = "Invalid format (MM:SS)"
        )

        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Duration format is incorrect!"))
    }

    @Test
    fun `zero value for count or duration should fail`() {

        val cases = listOf(
            mapOf("https://example.com" to TestCaseParams(false, 0L, 0L, "")) to false,
            mapOf("https://example.com" to TestCaseParams(false, 0L, 0L, "")) to true
        )
        for ((urlList, isDuration) in cases) {
            val result = validateLaunchTest(
                urlList = urlList,
                isDuration = isDuration,
                durationErrorMessage = null
            )

            assertFalse(result.isValid)
            assertTrue(result.errorMessage!!.contains("doesn't set!"))
            urlList.keys.forEach { assertTrue { result.errorMessage.contains(it) } }
        }
    }


    @Test
    fun `valid data should pass`() {
        val cases = listOf(
            mapOf("url" to TestCaseParams(false, 10L, 0L, "")) to false,
            mapOf("url" to TestCaseParams(false, 0L, 10L, "")) to true
        )
        for ((urlList, isDuration) in cases) {
            val result = validateLaunchTest(
                urlList = urlList,
                isDuration = isDuration,
                durationErrorMessage = null
            )

            assertTrue(result.isValid)
            assertNull(result.errorMessage)
        }
    }

    @Test
    fun `invalid input should show dialog`() {
        var dialogShown = false
        var message: String? = null

        val testCaseMock: TestCase = mock()

        handleLaunchTest(
            testCase = testCaseMock,
            isDuration = true,
            cancelFlag = { false },
            urlList = emptyMap(),
            durationErrorMessage = null,
            coroutineScope = TestScope(),
            onUpdateTestCase = {},
            updateProgress = {},
            updateShowDialog = { dialogShown = it },
            updateDialogMessage = { message = it }
        )

        assertTrue(dialogShown)
        assertNotNull(message)
    }
}
