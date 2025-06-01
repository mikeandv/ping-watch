package com.github.mikeandv.pingwatch

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilsTest {

    @Test
    fun `should convert zero milliseconds to 00_00`() {
        val result = convertMillisToTime(0)
        assertEquals("00:00", result)
    }

    @Test
    fun `should convert milliseconds less than one minute`() {
        val result = convertMillisToTime(45000)
        assertEquals("00:45", result)
    }

    @Test
    fun `should convert milliseconds to minutes and seconds`() {
        val result = convertMillisToTime(125000)
        assertEquals("02:05", result)
    }

    @Test
    fun `should handle exactly one minute`() {
        val result = convertMillisToTime(60000)
        assertEquals("01:00", result)
    }

    @Test
    fun `should handle edge case of multiple minutes and no seconds`() {
        val result = convertMillisToTime(180000)
        assertEquals("03:00", result)
    }
}