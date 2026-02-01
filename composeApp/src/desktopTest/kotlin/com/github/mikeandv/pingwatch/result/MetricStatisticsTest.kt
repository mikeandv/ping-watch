package com.github.mikeandv.pingwatch.result

import kotlin.test.*

class MetricStatisticsTest {

    // fromValues tests

    @Test
    fun `fromValues should return null for empty list`() {
        val result = MetricStatistics.fromValues(emptyList())
        assertNull(result)
    }

    @Test
    fun `fromValues should calculate min correctly`() {
        val values = listOf(10.0, 5.0, 15.0, 3.0, 20.0)
        val result = MetricStatistics.fromValues(values)

        assertNotNull(result)
        assertEquals(3.0, result.min)
    }

    @Test
    fun `fromValues should calculate max correctly`() {
        val values = listOf(10.0, 5.0, 15.0, 3.0, 20.0)
        val result = MetricStatistics.fromValues(values)

        assertNotNull(result)
        assertEquals(20.0, result.max)
    }

    @Test
    fun `fromValues should calculate avg correctly`() {
        val values = listOf(10.0, 20.0, 30.0)
        val result = MetricStatistics.fromValues(values)

        assertNotNull(result)
        assertEquals(20.0, result.avg)
    }

    @Test
    fun `fromValues should calculate median correctly for odd count`() {
        val values = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val result = MetricStatistics.fromValues(values)

        assertNotNull(result)
        assertEquals(3.0, result.median)
    }

    @Test
    fun `fromValues should calculate median correctly for even count`() {
        val values = listOf(1.0, 2.0, 3.0, 4.0)
        val result = MetricStatistics.fromValues(values)

        assertNotNull(result)
        assertTrue(result.median in 2.0..3.0)
    }

    @Test
    fun `fromValues should calculate p95 correctly`() {
        val values = (1..100).map { it.toDouble() }
        val result = MetricStatistics.fromValues(values)

        assertNotNull(result)
        assertEquals(96.0, result.p95)
    }

    @Test
    fun `fromValues should calculate p99 correctly`() {
        val values = (1..100).map { it.toDouble() }
        val result = MetricStatistics.fromValues(values)

        assertNotNull(result)
        assertEquals(100.0, result.p99)
    }

    @Test
    fun `fromValues should handle single value`() {
        val values = listOf(42.0)
        val result = MetricStatistics.fromValues(values)

        assertNotNull(result)
        assertEquals(42.0, result.min)
        assertEquals(42.0, result.max)
        assertEquals(42.0, result.avg)
        assertEquals(42.0, result.median)
        assertEquals(42.0, result.p95)
        assertEquals(42.0, result.p99)
    }

    @Test
    fun `fromValues should handle two values`() {
        val values = listOf(10.0, 20.0)
        val result = MetricStatistics.fromValues(values)

        assertNotNull(result)
        assertEquals(10.0, result.min)
        assertEquals(20.0, result.max)
        assertEquals(15.0, result.avg)
    }

    @Test
    fun `fromValues should handle unsorted input`() {
        val values = listOf(50.0, 10.0, 30.0, 20.0, 40.0)
        val result = MetricStatistics.fromValues(values)

        assertNotNull(result)
        assertEquals(10.0, result.min)
        assertEquals(50.0, result.max)
        assertEquals(30.0, result.avg)
        assertEquals(30.0, result.median)
    }

    @Test
    fun `fromValues should handle duplicate values`() {
        val values = listOf(10.0, 10.0, 10.0, 10.0)
        val result = MetricStatistics.fromValues(values)

        assertNotNull(result)
        assertEquals(10.0, result.min)
        assertEquals(10.0, result.max)
        assertEquals(10.0, result.avg)
        assertEquals(10.0, result.median)
    }

    // EMPTY constant tests

    @Test
    fun `EMPTY should have all zero values`() {
        assertEquals(0.0, MetricStatistics.EMPTY.min)
        assertEquals(0.0, MetricStatistics.EMPTY.max)
        assertEquals(0.0, MetricStatistics.EMPTY.avg)
        assertEquals(0.0, MetricStatistics.EMPTY.median)
        assertEquals(0.0, MetricStatistics.EMPTY.p95)
        assertEquals(0.0, MetricStatistics.EMPTY.p99)
    }
}
