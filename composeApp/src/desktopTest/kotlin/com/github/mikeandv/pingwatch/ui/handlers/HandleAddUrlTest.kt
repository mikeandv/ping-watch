package com.github.mikeandv.pingwatch.ui.handlers

import com.github.mikeandv.pingwatch.domain.TestCaseSettings
import kotlin.test.*

class HandleAddUrlTest {

    private val urlPattern = TestCaseSettings.DEFAULT_URL_PATTERN

    @Test
    fun `valid URL should be added and callbacks invoked`() {
        val url = "https://example.com"
        var addedUrls: List<String>? = null
        var resetCalled = false
        var errorMessage: String? = "initial"

        handleAddUrl(
            url = url,
            urlPattern = urlPattern,
            updateUrlList = { addedUrls = it },
            resetUrl = { resetCalled = true },
            updateErrorMessage = { errorMessage = it }
        )

        assertNotNull(addedUrls)
        assertEquals(1, addedUrls.size)
        assertTrue(addedUrls[0].startsWith("https://example.com"))
        assertTrue(resetCalled)
        assertNull(errorMessage)
    }

    @Test
    fun `invalid URL should set error message`() {
        val url = "invalid-url"
        var addedUrls: List<String>? = null
        var resetCalled = false
        var errorMessage: String? = null

        handleAddUrl(
            url = url,
            urlPattern = urlPattern,
            updateUrlList = { addedUrls = it },
            resetUrl = { resetCalled = true },
            updateErrorMessage = { errorMessage = it }
        )

        assertNull(addedUrls)
        assertFalse(resetCalled)
        assertEquals("Incorrect URL format", errorMessage)
    }

    @Test
    fun `URL with port should be valid`() {
        val url = "http://localhost:3000/api"
        var addedUrls: List<String>? = null
        var errorMessage: String? = "initial"

        handleAddUrl(
            url = url,
            urlPattern = urlPattern,
            updateUrlList = { addedUrls = it },
            resetUrl = {},
            updateErrorMessage = { errorMessage = it }
        )

        assertNotNull(addedUrls)
        assertNull(errorMessage)
    }

    @Test
    fun `IP address URL should be valid`() {
        val url = "http://192.168.1.1:8080/api"
        var addedUrls: List<String>? = null
        var errorMessage: String? = "initial"

        handleAddUrl(
            url = url,
            urlPattern = urlPattern,
            updateUrlList = { addedUrls = it },
            resetUrl = {},
            updateErrorMessage = { errorMessage = it }
        )

        assertNotNull(addedUrls)
        assertNull(errorMessage)
    }

    @Test
    fun `URL with path should be normalized`() {
        val url = "https://example.com/api/users"
        var addedUrls: List<String>? = null

        handleAddUrl(
            url = url,
            urlPattern = urlPattern,
            updateUrlList = { addedUrls = it },
            resetUrl = {},
            updateErrorMessage = {}
        )

        assertNotNull(addedUrls)
        assertTrue(addedUrls[0].contains("/api/users"))
    }
}
