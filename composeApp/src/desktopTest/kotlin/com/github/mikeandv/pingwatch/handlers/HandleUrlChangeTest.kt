package com.github.mikeandv.pingwatch.handlers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HandleUrlChangeTest {
    private val urlPattern = Regex("^https?://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/\\S*)?$")
    private val expectedUrl = "http://example.com"
    private val expectedErrorMsg = "URL must follow formats like `https://example.com`."


    @Test
    fun `should update url and keep error message is null`() {
        var url = ""
        var errorMsg: String? = null
        handleUrlChange(expectedUrl, { url = it }, { errorMsg = it }, urlPattern)

        assertEquals(expectedUrl, url)
        assertNull(errorMsg)
    }

    @Test
    fun `should update url and change error message to null`() {
        var url = ""
        var errorMsg: String? = "Some error"
        handleUrlChange(expectedUrl, { url = it }, { errorMsg = it }, urlPattern)

        assertEquals(expectedUrl, url)
        assertNull(errorMsg)
    }

    @Test
    fun `should update url but url is not matching pattern`() {
        var url = ""
        var errorMsg: String? = "Some error"
        handleUrlChange("test", { url = it }, { errorMsg = it }, urlPattern)

        assertEquals("test", url)
        assertEquals(expectedErrorMsg, errorMsg)
    }

    @Test
    fun `should update url but url is empty`() {
        var url = "test"
        var errorMsg: String? = "Some error"
        handleUrlChange("", { url = it }, { errorMsg = it }, urlPattern)

        assertEquals("", url)
        assertNull(errorMsg)
    }

}