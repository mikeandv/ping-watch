package com.github.mikeandv.pingwatch.ui.handlers

import com.github.mikeandv.pingwatch.domain.TestCaseParams
import com.github.mikeandv.pingwatch.ui.utils.CountInputResult
import com.github.mikeandv.pingwatch.ui.utils.IntInputResult
import com.github.mikeandv.pingwatch.ui.utils.LaunchValidationResult
import com.github.mikeandv.pingwatch.ui.utils.TimeInputResult
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

fun validateUrlsFile(
    lines: List<String>,
    maxLinesLimit: Int,
    urlPattern: Regex
): Result<List<String>> {

    if (lines.isEmpty()) {
        return Result.failure(Exception("There is no lines in file"))
    }

    if (lines.size > maxLinesLimit) {
        return Result.failure(Exception("Reach lines limit in file.\nLimit: $maxLinesLimit."))
    }

    if (lines.any { !it.matches(urlPattern) }) {
        return Result.failure(Exception("Some of urls have incorrect format"))
    }

    val normalizedUrls = mutableListOf<String>()
    val failedUrls = mutableListOf<String>()

    for (line in lines) {
        val normalized = line.toHttpUrlOrNull()?.toString()
        if (normalized != null) {
            normalizedUrls.add(normalized)
        } else {
            failedUrls.add(line)
        }
    }

    if (failedUrls.isNotEmpty()) {
        return Result.failure(Exception("Failed to normalize URLs:\n${failedUrls.joinToString("\n")}"))
    }

    return Result.success(normalizedUrls)
}

fun processTimeInput(input: String): TimeInputResult {

    if (input.isEmpty()) {
        return TimeInputResult.Empty()
    }

    val partialRegexes = listOf(
        Regex("^\\d$"),
        Regex("^\\d\\d$"),
        Regex("^\\d\\d:$"),
        Regex("^\\d\\d:\\d$")
    )

    if (partialRegexes.any { it.matches(input) }) {
        return TimeInputResult.Partial(input)
    }

    val fullRegex = Regex("^\\d\\d:\\d\\d$")

    if (!fullRegex.matches(input)) {
        return TimeInputResult.Error("Invalid format (MM:SS)")
    }

    val (mm, ss) = input.split(":")
    val minutes = mm.toIntOrNull() ?: return TimeInputResult.Error("Invalid format (MM:SS)")
    val seconds = ss.toIntOrNull() ?: return TimeInputResult.Error("Invalid format (MM:SS)")

    if (seconds !in 0..59) {
        return TimeInputResult.Error("Seconds must be in the range 00-59")
    }

    return TimeInputResult.Valid(
        unformatted = input,
        timeMillis = (minutes * 60 + seconds) * 1000L
    )
}

fun processCountInput(input: String): CountInputResult {
    if (input.isEmpty()) {
        return CountInputResult.Empty()
    }

    val number = input.toLongOrNull() ?: return CountInputResult.Error("Enter the number")

    return CountInputResult.Valid(number)
}

fun processIntInput(input: String, min: Int, max: Int, requiredMessage: String = "Required"): IntInputResult {
    if (input.isEmpty()) {
        return IntInputResult.Error(requiredMessage)
    }

    val value = input.toIntOrNull()
        ?: return IntInputResult.Error("Must be a number")

    return when {
        value < min -> IntInputResult.Error("Must be at least $min")
        value > max -> IntInputResult.Error("Must be at most $max")
        else -> IntInputResult.Valid(value)
    }
}

fun processParallelismInput(input: String): IntInputResult =
    processIntInput(input, min = 1, max = 64, requiredMessage = "Parallelism is required")

fun processMaxFileSizeInput(input: String): IntInputResult =
    processIntInput(input, min = 1, max = 100)

fun processMaxLinesLimitInput(input: String): IntInputResult =
    processIntInput(input, min = 1, max = 1000)

fun validateLaunchTest(
    urlList: Map<String, TestCaseParams>,
    isDuration: Boolean,
    durationErrorMessage: String?,
    parallelismError: String?
): LaunchValidationResult {

    val errors = mutableListOf<String>()

    if (urlList.isEmpty()) {
        errors.add("URLs list is empty!")
    }

    if (!durationErrorMessage.isNullOrEmpty()) {
        errors.add("Duration format is incorrect!")
    }

    if (!parallelismError.isNullOrEmpty()) {
        errors.add("Parallelism value is incorrect!")
    }

    if (isDuration && urlList.values.any { it.durationValue == 0L }) {
        errors.add(
            "Time duration doesn't set!\n" +
                    urlList.filter { it.value.durationValue == 0L }
                        .keys.joinToString(", ")
        )
    }

    if (!isDuration && urlList.values.any { it.countValue == 0L }) {
        errors.add(
            "Requests count doesn't set!\n" +
                    urlList.filter { it.value.countValue == 0L }
                        .keys.joinToString(", ")
        )
    }

    if (errors.isNotEmpty()) {
        return LaunchValidationResult(
            isValid = false,
            errorMessage = "Incorrect values for running the test:\n" +
                    errors.joinToString("\n")
        )
    }

    return LaunchValidationResult(isValid = true)
}
