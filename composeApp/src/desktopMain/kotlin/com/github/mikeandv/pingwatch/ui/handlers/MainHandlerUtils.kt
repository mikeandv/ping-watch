package com.github.mikeandv.pingwatch.ui.handlers

import com.github.mikeandv.pingwatch.domain.ExecutionMode
import com.github.mikeandv.pingwatch.domain.RunType
import com.github.mikeandv.pingwatch.domain.TestCase
import com.github.mikeandv.pingwatch.ui.utils.*
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

fun processCountInput(input: String, min: Int, max: Int): CountInputResult {
    if (input.isEmpty()) {
        return CountInputResult.Empty()
    }

    val number = input.toLongOrNull() ?: return CountInputResult.Error("Must be a number")

    return when {
        number < min -> CountInputResult.Error("Must be at least $min")
        number > max -> CountInputResult.Error("Must be at most $max")
        else -> CountInputResult.Valid(number)
    }
}

fun processParallelismInput(input: String, min: Int, max: Int): ParallelismInputResult {
    if (input.isEmpty()) {
        return ParallelismInputResult.Empty()
    }

    val value = input.toIntOrNull()
        ?: return ParallelismInputResult.Error("Must be a number")

    return when {
        value < min -> ParallelismInputResult.Error("Must be at least $min")
        value > max -> ParallelismInputResult.Error("Must be at most $max")
        else -> ParallelismInputResult.Valid(value)
    }
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

fun validateLaunchTest(
    testCase: TestCase,
    durationErrorMessage: String?,
    parallelismError: String?
): LaunchValidationResult {

    val errors = mutableListOf<String>()

    if (testCase.urls.isEmpty()) {
        errors.add("URLs list is empty!")
    }

    if (!durationErrorMessage.isNullOrEmpty()) {
        errors.add("Duration format is incorrect!")
    }

    if (testCase.executionMode == ExecutionMode.PARALLEL && !parallelismError.isNullOrEmpty()) {
        errors.add("Parallelism value is incorrect!")
    }

    if (testCase.executionMode == ExecutionMode.PARALLEL && testCase.parallelism == 0) {
        errors.add("Parallelism doesn't set!")
    }

    if (testCase.runType == RunType.DURATION && testCase.urls.values.any { it.durationValue == 0L }) {
        errors.add(
            "Time duration doesn't set!\n" +
                    testCase.urls.filter { it.value.durationValue == 0L }
                        .keys.joinToString(", ")
        )
    }

    if (testCase.runType == RunType.COUNT && testCase.urls.values.any { it.countValue == 0L }) {
        errors.add(
            "Requests count doesn't set!\n" +
                    testCase.urls.filter { it.value.countValue == 0L }
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
