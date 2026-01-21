package com.github.mikeandv.pingwatch.handlers

import com.github.mikeandv.pingwatch.entity.CountInputResult
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class ProcessCountInputTest {

    @Test
    fun `empty input should return empty result`() {
        val result = processCountInput("")

        assertTrue(result is CountInputResult.Empty)
        assertEquals(0L, result.value)
    }

    @Test
    fun `valid numbers should return valid result`() {
        val cases = listOf(
            "0" to 0L,
            "1" to 1L,
            "10" to 10L,
            "999" to 999L,
            "123456" to 123456L
        )

        for ((input, expected) in cases) {
            val result = processCountInput(input)

            assertTrue(result is CountInputResult.Valid, "input=$input")
            assertEquals(expected, result.value)
        }
    }

    @Test
    fun `invalid input should return error result`() {
        val cases = listOf(
            "abc",
            "12a",
            "-",
            "1.5",
            " ",
            "++",
            "01a"
        )

        for (input in cases) {
            val result = processCountInput(input)

            assertTrue(result is CountInputResult.Error, "input=$input")
            assertEquals("Enter the number", result.message)
        }
    }
}
