package com.github.mikeandv.pingwatch.handlers

import com.github.mikeandv.pingwatch.entity.TestCaseParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class HandleAddUrlTest {

    private val urlPattern = Regex("^https?://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/\\S*)?$")

    @Test
    fun `valid url should be added and reset called`() {
        val currentUrls = emptyMap<String, TestCaseParams>()
        val url = "https://example.com"

        var updatedUrls: Map<String, TestCaseParams>? = null
        var resetCalled = false
        var errorMessage: String? = "something"

        handleAddUrl(
            url = url,
            urlPattern = urlPattern,
            updateUrlList = { updatedUrls = it },
            currentUrls = currentUrls,
            resetUrl = { resetCalled = true },
            updateErrorMessage = { errorMessage = it }
        )

        assertNotNull(updatedUrls)
        assertTrue(updatedUrls.containsKey(url))
        assertTrue(resetCalled)
        assertNull(errorMessage)
    }

    @Test
    fun `invalid url should not be added and error shown`() {

        val currentUrls = emptyMap<String, TestCaseParams>()
        val url = "invalid_url"

        var updatedUrlsCalled = false
        var resetCalled = false
        var errorMessage: String? = null

        handleAddUrl(
            url = url,
            urlPattern = urlPattern,
            updateUrlList = {
                updatedUrlsCalled = true
            },
            currentUrls = currentUrls,
            resetUrl = { resetCalled = true },
            updateErrorMessage = { errorMessage = it }
        )

        assertFalse(updatedUrlsCalled)
        assertFalse(resetCalled)
        assertEquals("Incorrect URL", errorMessage)
    }

    @Test
    fun `adding url should preserve existing urls`() {
        val existingUrl = "https://existing.com"
        val newUrl = "https://new.com"

        val currentUrls = mapOf(
            existingUrl to TestCaseParams(false, 0L, 0L, "")
        )

        var updatedUrls: Map<String, TestCaseParams>? = null


        handleAddUrl(
            url = newUrl,
            urlPattern = urlPattern,
            updateUrlList = { updatedUrls = it },
            currentUrls = currentUrls,
            resetUrl = {},
            updateErrorMessage = {}
        )

        assertNotNull(updatedUrls)
        assertEquals(2, updatedUrls.size)
        assertTrue(updatedUrls.containsKey(existingUrl))
        assertTrue(updatedUrls.containsKey(newUrl))
    }

    @Test
    fun `new url should be added with default TestCaseParams`() {
        val url = "https://example.com"
        val currentUrls = emptyMap<String, TestCaseParams>()

        var updatedUrls: Map<String, TestCaseParams>? = null

        handleAddUrl(
            url = url,
            urlPattern = urlPattern,
            updateUrlList = { updatedUrls = it },
            currentUrls = currentUrls,
            resetUrl = {},
            updateErrorMessage = {}
        )

        val params = updatedUrls!![url]
        assertEquals(false, params!!.isEdit)
        assertEquals(0L, params.countValue)
        assertEquals(0L, params.durationValue)
        assertEquals("", params.unformattedDurationValue)
    }
}
