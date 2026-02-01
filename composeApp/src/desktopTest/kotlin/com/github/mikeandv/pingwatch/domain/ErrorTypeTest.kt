package com.github.mikeandv.pingwatch.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorTypeTest {

    // fromStatusCode tests

    @Test
    fun `fromStatusCode should return NONE for success status codes`() {
        listOf(
            200,
            201,
            204,
            301,
            304
        ).forEach {
            assertEquals(ErrorType.NONE, ErrorType.fromStatusCode(it))
        }
    }

    @Test
    fun `fromStatusCode should return HTTP_CRITICAL_ERROR for status codes`() {
        listOf(
            400,
            401,
            403,
            404,
            410
        ).forEach {
            assertEquals(ErrorType.HTTP_CRITICAL_ERROR, ErrorType.fromStatusCode(it))
        }
    }

    @Test
    fun `fromStatusCode should return HTTP_CLIENT_ERROR for status codes`() {
        listOf(
            405,
            429,
        ).forEach {
            assertEquals(ErrorType.HTTP_CLIENT_ERROR, ErrorType.fromStatusCode(it))
        }
    }

    @Test
    fun `fromStatusCode should return HTTP_SERVER_ERROR for status codes`() {
        listOf(
            500,
            502,
            503,
            504
        ).forEach {
            assertEquals(ErrorType.HTTP_SERVER_ERROR, ErrorType.fromStatusCode(it))
        }
    }


    @Test
    fun `fromStatusCode should return HTTP_SERVER_ERROR unknown status code`() {
        listOf(
            600,
            100,
        ).forEach {
            assertEquals(ErrorType.NONE, ErrorType.fromStatusCode(it))
        }
    }
}
