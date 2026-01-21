package com.github.mikeandv.pingwatch.handlers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ValidateUrlsFileTest {
    private val urlPattern = Regex("^https?://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/\\S*)?$")


    @Test
    fun `empty file should return error`() {
        val result = validateUrlsFile(emptyList(), 20, urlPattern)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invalid url should return error`() {
        val lines = listOf("http://good.com", "not_a_url")

        val result = validateUrlsFile(lines, 20, urlPattern)

        assertTrue(result.isFailure)
    }

    @Test
    fun `valid file should return map`() {
        val lines = listOf("https://a.com", "https://b.com")

        val result = validateUrlsFile(lines, 20, urlPattern)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
    }


}