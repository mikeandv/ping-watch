package com.github.mikeandv.pingwatch.utils

import com.github.mikeandv.pingwatch.domain.Category
import com.github.mikeandv.pingwatch.domain.StatusCode
import com.github.mikeandv.pingwatch.domain.TestCaseParams

fun convertMillisToTime(millis: Long): String {
    val seconds = millis / 1000 % 60
    val minutes = millis / 1000 / 60 % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun checkIsNotRunningStatus(status: StatusCode): Boolean {
    return status == StatusCode.FINISHED || status == StatusCode.CREATED
}

fun getNewTagId(tags: List<Category>) = (tags.maxOfOrNull { it.id } ?: 0) + 1

fun getCategory(urls: Map<String, TestCaseParams>, targetUrl: String): Category? = urls[targetUrl]?.tag

