package com.github.mikeandv.pingwatch

enum class StatusCode {
    CREATED, RUNNING, FINISHED, STOPPED
}

enum class RunType {
    DURATION, COUNT
}
fun convertMillisToTime(millis: Long):String {
    val seconds =  millis / 1000 % 60
    val minutes = millis / 1000 / 60 % 60
    return String.format("%02d:%02d", minutes, seconds)
}
