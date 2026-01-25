package com.github.mikeandv.pingwatch.utils

import com.github.mikeandv.pingwatch.domain.StatusCode

fun convertMillisToTime(millis: Long): String {
    val seconds = millis / 1000 % 60
    val minutes = millis / 1000 / 60 % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun checkIsNotRunningStatus(status: StatusCode): Boolean {
    return status == StatusCode.FINISHED || status == StatusCode.CREATED
}
