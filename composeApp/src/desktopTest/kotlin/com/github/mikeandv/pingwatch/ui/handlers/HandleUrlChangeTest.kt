package com.github.mikeandv.pingwatch.ui.handlers

import com.github.mikeandv.pingwatch.domain.TestCaseSettings
import kotlin.test.*

class HandleUrlChangeTest {

    private val urlPattern = TestCaseSettings.DEFAULT_URL_PATTERN

    @Test
    fun `valid URL should clear error message`() {
        val input = "https://example.com"
        var updatedUrl: String? = null
        var errorMessage: String? = "initial"

        handleUrlChange(
            input = input,
            updateUrl = { updatedUrl = it },
            updateErrorMessage = { errorMessage = it },
            urlPattern = urlPattern
        )

        assertEquals(input, updatedUrl)
        assertNull(errorMessage)
    }

    @Test
    fun `empty input should clear error message`() {
        val input = ""
        var updatedUrl: String? = null
        var errorMessage: String? = "initial"

        handleUrlChange(
            input = input,
            updateUrl = { updatedUrl = it },
            updateErrorMessage = { errorMessage = it },
            urlPattern = urlPattern
        )

        assertEquals("", updatedUrl)
        assertNull(errorMessage)
    }

    @Test
    fun `invalid URL should set error message`() {
        val input = "not-a-valid-url"
        var updatedUrl: String? = null
        var errorMessage: String? = null

        handleUrlChange(
            input = input,
            updateUrl = { updatedUrl = it },
            updateErrorMessage = { errorMessage = it },
            urlPattern = urlPattern
        )

        assertEquals(input, updatedUrl)
        assertNotNull(errorMessage)
        assertTrue(errorMessage!!.contains("URL must follow formats"))
    }

    @Test
    fun `partial URL should set error message`() {
        val input = "https://"
        var errorMessage: String? = null

        handleUrlChange(
            input = input,
            updateUrl = {},
            updateErrorMessage = { errorMessage = it },
            urlPattern = urlPattern
        )

        assertNotNull(errorMessage)
    }

    @Test
    fun `localhost URL should be valid`() {
        val input = "http://localhost:3000"
        var errorMessage: String? = "initial"

        handleUrlChange(
            input = input,
            updateUrl = {},
            updateErrorMessage = { errorMessage = it },
            urlPattern = urlPattern
        )

        assertNull(errorMessage)
    }

    @Test
    fun `IP address URL should be valid`() {
        val input = "http://192.168.1.100:8080/api"
        var errorMessage: String? = "initial"

        handleUrlChange(
            input = input,
            updateUrl = {},
            updateErrorMessage = { errorMessage = it },
            urlPattern = urlPattern
        )

        assertNull(errorMessage)
    }
}
