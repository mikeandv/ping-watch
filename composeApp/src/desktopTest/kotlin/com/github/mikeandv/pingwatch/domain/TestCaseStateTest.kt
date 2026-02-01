package com.github.mikeandv.pingwatch.domain

import kotlin.test.*

class TestCaseStateTest {

    @Test
    fun `initial status should be CREATED`() {
        val state = TestCaseState()
        assertEquals(StatusCode.CREATED, state.getStatus())
    }

    @Test
    fun `startTestCase should change status to RUNNING`() {
        val state = TestCaseState()
        state.startTestCase()
        assertEquals(StatusCode.RUNNING, state.getStatus())
    }

    @Test
    fun `finishTestCase should change status to FINISHED`() {
        val state = TestCaseState()
        state.startTestCase()
        state.finishTestCase()
        assertEquals(StatusCode.FINISHED, state.getStatus())
    }

    @Test
    fun `status flow should reflect current status`() {
        val state = TestCaseState()
        assertEquals(StatusCode.CREATED, state.status.value)

        state.startTestCase()
        assertEquals(StatusCode.RUNNING, state.status.value)

        state.finishTestCase()
        assertEquals(StatusCode.FINISHED, state.status.value)
    }

    @Test
    fun `getStatus should return same value as status flow`() {
        val state = TestCaseState()

        assertEquals(state.status.value, state.getStatus())

        state.startTestCase()
        assertEquals(state.status.value, state.getStatus())

        state.finishTestCase()
        assertEquals(state.status.value, state.getStatus())
    }
}
