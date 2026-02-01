package com.github.mikeandv.pingwatch.ui.handlers

import com.github.mikeandv.pingwatch.ui.utils.TimeInputResult
import kotlin.test.*

class ProcessTimeInputTest {

    @Test
    fun `empty input should return Empty`() {
        val result = processTimeInput("")
        assertTrue(result is TimeInputResult.Empty)
    }

    @Test
    fun `single digit should return Partial`() {
        val result = processTimeInput("1")
        assertTrue(result is TimeInputResult.Partial)
        assertEquals("1", result.unformatted)
    }

    @Test
    fun `two digits should return Partial`() {
        val result = processTimeInput("12")
        assertTrue(result is TimeInputResult.Partial)
        assertEquals("12", result.unformatted)
    }

    @Test
    fun `two digits with colon should return Partial`() {
        val result = processTimeInput("12:")
        assertTrue(result is TimeInputResult.Partial)
        assertEquals("12:", result.unformatted)
    }

    @Test
    fun `three digits with colon should return Partial`() {
        val result = processTimeInput("12:3")
        assertTrue(result is TimeInputResult.Partial)
        assertEquals("12:3", result.unformatted)
    }

    @Test
    fun `valid MM SS format should return Valid`() {
        val result = processTimeInput("05:30")
        assertTrue(result is TimeInputResult.Valid)
        assertEquals("05:30", result.unformatted)
        assertEquals(330000L, result.timeMillis)
    }

    @Test
    fun `valid 00 00 should return Valid with 0 millis`() {
        val result = processTimeInput("00:00")
        assertTrue(result is TimeInputResult.Valid)
        assertEquals(0L, result.timeMillis)
    }

    @Test
    fun `valid 01 00 should return 60000 millis`() {
        val result = processTimeInput("01:00")
        assertTrue(result is TimeInputResult.Valid)
        assertEquals(60000L, result.timeMillis)
    }

    @Test
    fun `valid 59 59 should return correct millis`() {
        val result = processTimeInput("59:59")
        assertTrue(result is TimeInputResult.Valid)
        assertEquals((59 * 60 + 59) * 1000L, result.timeMillis)
    }

    @Test
    fun `seconds above 59 should return Error`() {
        val result = processTimeInput("01:60")
        assertTrue(result is TimeInputResult.Error)
        assertEquals("Seconds must be in the range 00-59", result.message)
    }

    @Test
    fun `seconds 99 should return Error`() {
        val result = processTimeInput("01:99")
        assertTrue(result is TimeInputResult.Error)
    }

    @Test
    fun `invalid format without colon should return Error`() {
        val result = processTimeInput("12345")
        assertTrue(result is TimeInputResult.Error)
        assertEquals("Invalid format (MM:SS)", result.message)
    }

    @Test
    fun `invalid format with letters should return Error`() {
        val result = processTimeInput("ab:cd")
        assertTrue(result is TimeInputResult.Error)
    }

    @Test
    fun `invalid format with extra characters should return Error`() {
        val result = processTimeInput("12:34:56")
        assertTrue(result is TimeInputResult.Error)
    }
}
