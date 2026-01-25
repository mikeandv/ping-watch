package com.github.mikeandv.pingwatch.domain

data class ResponseData(val url: String, val statusCode: Int, val duration: Long, val errorMessage: String)