package com.github.mikeandv.pingwatch.handlers

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.entity.*

fun validateUrlsFile(
    lines: List<String>,
    maxLinesLimit: Int,
    urlPattern: Regex
): Result<Map<String, TestCaseParams>> {

    if (lines.isEmpty()) {
        return Result.failure(Exception("There is no lines in file"))
    }

    if (lines.size > maxLinesLimit) {
        return Result.failure(Exception("Reach lines limit in file.\nLimit: $maxLinesLimit."))
    }

    if (lines.any { !it.matches(urlPattern) }) {
        return Result.failure(Exception("Some of urls have incorrect format"))
    }

    return Result.success(
        lines.associateWith { TestCaseParams(false, 0L, 0L, "") }
    )
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

fun validateLaunchTest(
    urlList: Map<String, TestCaseParams>,
    isDuration: Boolean,
    durationErrorMessage: String?
): LaunchValidationResult {

    val errors = mutableListOf<String>()

    if (urlList.isEmpty()) {
        errors.add("URLs list is empty!")
    }

    if (!durationErrorMessage.isNullOrEmpty()) {
        errors.add("Duration format is incorrect!")
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

fun buildTestCase(
    original: TestCase,
    urlList: Map<String, TestCaseParams>,
    isDuration: Boolean
): TestCase {
    return TestCase(
        urlList,
        if (isDuration) RunType.DURATION else RunType.COUNT,
        original.settings,
        original.testCaseState,
        original.testCaseResult
    )
}


