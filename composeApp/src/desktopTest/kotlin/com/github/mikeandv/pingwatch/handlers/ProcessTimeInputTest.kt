package com.github.mikeandv.pingwatch.handlers

import com.github.mikeandv.pingwatch.entity.TimeInputResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProcessTimeInputTest {

    @Test
    fun `valid inputs should return valid result`() {
        val cases = listOf(
            "00:00" to 0L,
            "00:01" to 1_000L,
            "01:00" to 60_000L,
            "02:30" to 150_000L,
            "59:59" to 3_599_000L
        )

        for ((input, expected) in cases) {
            val result = processTimeInput(input)

            assertTrue(result is TimeInputResult.Valid, "input=${input}")
            assertEquals(input, result.unformatted)
            assertEquals(expected, result.timeMillis)
        }
    }

    @Test
    fun `partial inputs should return partial result`() {
        val cases = listOf(
            "1",
            "12",
            "12:",
            "12:3"
        )

        for (input in cases) {
            val result = processTimeInput(input)

            assertTrue(result is TimeInputResult.Partial, "input=$input")
            assertEquals(input, result.unformatted)
            assertEquals("Invalid format (MM:SS)", result.error)
        }
    }

    @Test
    fun `invalid inputs should return error result`() {
        val cases = listOf(
            "1:23",
            "12345",
            "ab:cd",
            "12:99",
            "99:99",
            "aa",
            ":12",
            "12::"
        )

        for (input in cases) {
            val result = processTimeInput(input)

            assertTrue(result is TimeInputResult.Error, "input=$input")
            assertTrue(result.message.isNotBlank())
        }
    }

    @Test
    fun `empty input should return empty result`() {
        val result = processTimeInput("")

        assertTrue(result is TimeInputResult.Empty)
        assertEquals(0L, result.timeMillis)
        assertEquals("", result.unformatted)
    }
}



