package com.github.mikeandv.pingwatch.utils

import com.github.mikeandv.pingwatch.domain.Category
import com.github.mikeandv.pingwatch.domain.StatusCode
import com.github.mikeandv.pingwatch.domain.TestCaseParams
import kotlin.test.*

class UtilsTest {

    // convertMillisToTime tests

    @Test
    fun `convertMillisToTime should convert zero milliseconds to 00_00`() {
        val result = convertMillisToTime(0)
        assertEquals("00:00", result)
    }

    @Test
    fun `convertMillisToTime should convert milliseconds less than one minute`() {
        val result = convertMillisToTime(45000)
        assertEquals("00:45", result)
    }

    @Test
    fun `convertMillisToTime should convert milliseconds to minutes and seconds`() {
        val result = convertMillisToTime(125000)
        assertEquals("02:05", result)
    }

    @Test
    fun `convertMillisToTime should handle exactly one minute`() {
        val result = convertMillisToTime(60000)
        assertEquals("01:00", result)
    }

    @Test
    fun `convertMillisToTime should handle multiple minutes and no seconds`() {
        val result = convertMillisToTime(180000)
        assertEquals("03:00", result)
    }

    @Test
    fun `convertMillisToTime should handle large values`() {
        val result = convertMillisToTime(3599000)
        assertEquals("59:59", result)
    }

    @Test
    fun `convertMillisToTime should wrap after 60 minutes`() {
        val result = convertMillisToTime(3660000)
        assertEquals("01:00", result)
    }

    // checkIsNotRunningStatus tests

    @Test
    fun `checkIsNotRunningStatus should return true for CREATED`() {
        assertTrue(checkIsNotRunningStatus(StatusCode.CREATED))
    }

    @Test
    fun `checkIsNotRunningStatus should return true for FINISHED`() {
        assertTrue(checkIsNotRunningStatus(StatusCode.FINISHED))
    }

    @Test
    fun `checkIsNotRunningStatus should return false for RUNNING`() {
        assertFalse(checkIsNotRunningStatus(StatusCode.RUNNING))
    }

    // getNewTagId tests

    @Test
    fun `getNewTagId should return 1 for empty list`() {
        val result = getNewTagId(emptyList())
        assertEquals(1, result)
    }

    @Test
    fun `getNewTagId should return max id plus 1`() {
        val tags = listOf(
            Category(id = 1, name = "Tag1"),
            Category(id = 3, name = "Tag3"),
            Category(id = 2, name = "Tag2")
        )
        val result = getNewTagId(tags)
        assertEquals(4, result)
    }

    @Test
    fun `getNewTagId should handle single tag`() {
        val tags = listOf(Category(id = 5, name = "Tag"))
        val result = getNewTagId(tags)
        assertEquals(6, result)
    }

    // getCategory tests

    @Test
    fun `getCategory should return category for existing URL`() {
        val category = Category(id = 1, name = "API")
        val params = TestCaseParams(
            isEdit = false,
            countValue = 10,
            durationValue = 0,
            unformattedDurationValue = "",
            tag = category
        )
        val urls = mapOf("https://example.com" to params)

        val result = getCategory(urls, "https://example.com")

        assertEquals(category, result)
    }

    @Test
    fun `getCategory should return null for URL without category`() {
        val params = TestCaseParams(
            isEdit = false,
            countValue = 10,
            durationValue = 0,
            unformattedDurationValue = "",
            tag = null
        )
        val urls = mapOf("https://example.com" to params)

        val result = getCategory(urls, "https://example.com")

        assertNull(result)
    }

    @Test
    fun `getCategory should return null for non-existent URL`() {
        val urls = emptyMap<String, TestCaseParams>()

        val result = getCategory(urls, "https://example.com")

        assertNull(result)
    }
}
