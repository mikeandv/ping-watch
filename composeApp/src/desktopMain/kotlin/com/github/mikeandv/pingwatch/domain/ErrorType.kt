package com.github.mikeandv.pingwatch.domain

import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

enum class ErrorType {
    NONE,                    // No error (success 2xx-3xx)

    // Network errors
    CONNECTION_REFUSED,      // Server refused connection
    HOST_UNREACHABLE,        // Cannot reach host
    TIMEOUT,                 // Connection or read timeout
    DNS_FAILURE,             // DNS resolution failed
    SSL_ERROR,               // TLS/SSL handshake failure
    NETWORK_ERROR;           // Other IOException

    companion object {
        fun fromException(e: IOException): ErrorType {
            return when (e) {
                is SocketTimeoutException -> TIMEOUT
                is ConnectException -> CONNECTION_REFUSED
                is UnknownHostException -> DNS_FAILURE
                is NoRouteToHostException -> HOST_UNREACHABLE
                is SSLException -> SSL_ERROR
                else -> NETWORK_ERROR
            }
        }
    }
}
