package com.github.mikeandv.pingwatch.ui.handlers

import com.github.mikeandv.pingwatch.domain.TestCaseSettings
import kotlin.test.*

class ValidateUrlsFileTest {

    private val urlPattern = TestCaseSettings.DEFAULT_URL_PATTERN

    @Test
    fun `empty lines should return failure`() {
        val result = validateUrlsFile(emptyList(), 100, urlPattern)
        assertTrue(result.isFailure)
        assertEquals("There is no lines in file", result.exceptionOrNull()?.message)
    }

    @Test
    fun `lines exceeding limit should return failure`() {
        val lines = List(101) { "https://example$it.com" }
        val result = validateUrlsFile(lines, 100, urlPattern)
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message?.contains("Reach lines limit"), true)
    }

    @Test
    fun `valid URLs should return success`() {
        val lines = listOf(
            "https://example.com",
            "https://test.example.com",
            "http://another.org"
        )
        val result = validateUrlsFile(lines, 100, urlPattern)
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
    }

    @Test
    fun `invalid URL format should return failure`() {
        val lines = listOf(
            "https://example.com",
            "invalid-url",
            "https://test.com"
        )
        val result = validateUrlsFile(lines, 100, urlPattern)
        assertTrue(result.isFailure)
        assertEquals("Some of urls have incorrect format", result.exceptionOrNull()?.message)
    }

    @Test
    fun `single valid URL should return success`() {
        val lines = listOf("https://example.com")
        val result = validateUrlsFile(lines, 100, urlPattern)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `URLs at limit should return success`() {
        val lines = List(100) { "https://example$it.com" }
        val result = validateUrlsFile(lines, 100, urlPattern)
        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.size)
    }

    @Test
    fun `URLs with paths should be valid`() {
        val lines = listOf(
            "https://example.com/path/to/resource",
            "https://api.example.com/v1/users"
        )
        val result = validateUrlsFile(lines, 100, urlPattern)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `URLs with ports should be valid`() {
        val lines = listOf(
            "https://example.com:8080",
            "http://localhost:3000/api"
        )
        val result = validateUrlsFile(lines, 100, urlPattern)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `IP addresses should be valid`() {
        val lines = listOf(
            "http://192.168.1.1:8080/api",
            "https://10.0.0.1/endpoint"
        )
        val result = validateUrlsFile(lines, 100, urlPattern)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `normalized URLs should be returned`() {
        val lines = listOf("https://example.com")
        val result = validateUrlsFile(lines, 100, urlPattern)
        assertTrue(result.isSuccess)
        val urls = result.getOrNull()
        assertNotNull(urls)
        assertTrue(urls[0].endsWith("/") || urls[0] == "https://example.com/")
    }
}
