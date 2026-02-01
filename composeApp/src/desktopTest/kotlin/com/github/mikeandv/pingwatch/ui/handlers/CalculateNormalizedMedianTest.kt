package com.github.mikeandv.pingwatch.ui.handlers

import kotlin.test.*

class CalculateNormalizedMedianTest {

    @Test
    fun `should return 100 when median equals maxMedian`() {
        val result = calculateNormalizedMedian(50.0, 50.0)
        assertEquals(100.0, result)
    }

    @Test
    fun `should return 50 when median is half of maxMedian`() {
        val result = calculateNormalizedMedian(25.0, 50.0)
        assertEquals(50.0, result)
    }

    @Test
    fun `should return 0 when median is 0`() {
        val result = calculateNormalizedMedian(0.0, 50.0)
        assertEquals(0.0, result)
    }

    @Test
    fun `should return 0 when maxMedian is 0`() {
        val result = calculateNormalizedMedian(50.0, 0.0)
        assertEquals(0.0, result)
    }

    @Test
    fun `should handle small fractions`() {
        val result = calculateNormalizedMedian(1.0, 100.0)
        assertEquals(1.0, result)
    }

    @Test
    fun `should handle large values`() {
        val result = calculateNormalizedMedian(5000.0, 10000.0)
        assertEquals(50.0, result)
    }

    @Test
    fun `should return correct percentage for decimal values`() {
        val result = calculateNormalizedMedian(33.33, 100.0)
        assertEquals(33.33, result, 0.001)
    }

    @Test
    fun `should handle negative maxMedian as zero division`() {
        // Negative maxMedian should still avoid division by zero
        val result = calculateNormalizedMedian(50.0, -10.0)
        assertEquals(0.0, result)
    }
}
