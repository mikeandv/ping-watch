package com.github.mikeandv.pingwatch.ui.handlers

import com.github.mikeandv.pingwatch.ui.utils.NumberInputResult
import kotlin.test.*

class ProcessNumberInputTest {

    // Core validation logic tests (using processNumberInput directly)

    @Test
    fun `empty input with allowEmpty=true should return Empty`() {
        val result = processNumberInput("", 1, 100, 0, { it.toInt() }, allowEmpty = true)
        assertTrue(result is NumberInputResult.Empty)
        assertEquals(0, result.value)
    }

    @Test
    fun `empty input with allowEmpty=false should return Error`() {
        val result = processNumberInput("", 1, 100, 0, { it.toInt() }, allowEmpty = false)
        assertTrue(result is NumberInputResult.Error)
        assertEquals("Required", result.message)
    }

    @Test
    fun `empty input with custom required message should return Error with message`() {
        val result = processNumberInput("", 1, 100, 0, { it.toInt() }, allowEmpty = false, "Custom message")
        assertTrue(result is NumberInputResult.Error)
        assertEquals("Custom message", result.message)
    }

    @Test
    fun `valid number within range should return Valid`() {
        val result = processNumberInput("50", 1, 100, 0, { it.toInt() })
        assertTrue(result is NumberInputResult.Valid)
        assertEquals(50, result.value)
    }

    @Test
    fun `number at min boundary should return Valid`() {
        val result = processNumberInput("1", 1, 100, 0, { it.toInt() })
        assertTrue(result is NumberInputResult.Valid)
        assertEquals(1, result.value)
    }

    @Test
    fun `number at max boundary should return Valid`() {
        val result = processNumberInput("100", 1, 100, 0, { it.toInt() })
        assertTrue(result is NumberInputResult.Valid)
        assertEquals(100, result.value)
    }

    @Test
    fun `number below min should return Error`() {
        val result = processNumberInput("0", 1, 100, 0, { it.toInt() })
        assertTrue(result is NumberInputResult.Error)
        assertEquals("Must be at least 1", result.message)
    }

    @Test
    fun `number above max should return Error`() {
        val result = processNumberInput("101", 1, 100, 0, { it.toInt() })
        assertTrue(result is NumberInputResult.Error)
        assertEquals("Must be at most 100", result.message)
    }

    @Test
    fun `non-numeric input should return Error`() {
        val result = processNumberInput("abc", 1, 100, 0, { it.toInt() })
        assertTrue(result is NumberInputResult.Error)
        assertEquals("Must be a number", result.message)
    }

    @Test
    fun `decimal number should return Error`() {
        val result = processNumberInput("50.5", 1, 100, 0, { it.toInt() })
        assertTrue(result is NumberInputResult.Error)
        assertEquals("Must be a number", result.message)
    }

    @Test
    fun `negative number should return Error when min is positive`() {
        val result = processNumberInput("-5", 1, 100, 0, { it.toInt() })
        assertTrue(result is NumberInputResult.Error)
        assertEquals("Must be at least 1", result.message)
    }

    @Test
    fun `negative number should return Valid when in range`() {
        val result = processNumberInput("-5", -10, 10, 0, { it.toInt() })
        assertTrue(result is NumberInputResult.Valid)
        assertEquals(-5, result.value)
    }

    // Wrapper function tests (verify correct type conversion)

    @Test
    fun `processCountInput should return Long value`() {
        val result = processCountInput("50", 1, 100)
        assertTrue(result is NumberInputResult.Valid)
        assertEquals(50L, result.value)
    }

    @Test
    fun `processCountInput empty should return Empty with 0L`() {
        val result = processCountInput("", 1, 100)
        assertTrue(result is NumberInputResult.Empty)
        assertEquals(0L, result.value)
    }

    @Test
    fun `processParallelismInput should return Int value`() {
        val result = processParallelismInput("8", 1, 64)
        assertTrue(result is NumberInputResult.Valid)
        assertEquals(8, result.value)
    }

    @Test
    fun `processParallelismInput empty should return Empty with 0`() {
        val result = processParallelismInput("", 1, 64)
        assertTrue(result is NumberInputResult.Empty)
        assertEquals(0, result.value)
    }

    @Test
    fun `processIntInput should return Int value`() {
        val result = processIntInput("50", 1, 100)
        assertTrue(result is NumberInputResult.Valid)
        assertEquals(50, result.value)
    }

    @Test
    fun `processIntInput empty should return Error`() {
        val result = processIntInput("", 1, 100)
        assertTrue(result is NumberInputResult.Error)
        assertEquals("Required", result.message)
    }
}
