package com.github.mikeandv.pingwatch.handlers

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.StatusCode
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.IOException
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

private val okHttpClient = OkHttpClient()

fun handleUrlChange(
    input: String,
    updateUrl: (String) -> Unit,
    updateErrorMessage: (String?) -> Unit,
    urlPattern: Regex
) {
    updateUrl(input)
    updateErrorMessage(
        if (input.matches(urlPattern) || input.isEmpty()) null else "URL must follow formats like `https://example.com`."
    )
}

fun handleImport(
    updateUrlList: (Map<String, TestCaseParams>) -> Unit,
    updateShowDialog: (Boolean) -> Unit,
    updateDialogErrorMessage: (String?) -> Unit,
    urlPattern: Regex
) {
    val maxFileSizeBytes = 5 * 1024 * 1024 // file size 5mb limit
    val maxLinesLimit = 20  // limit to lines in file
    val fileChooser = JFileChooser()
    val filter = FileNameExtensionFilter("Text File (*.txt)", "txt")
    fileChooser.fileFilter = filter

    val result = fileChooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        val file = fileChooser.selectedFile

        if (file.length() <= maxFileSizeBytes) {
            try {
                val urls = file.readLines() // Read the file as lines
                if (urls.size > maxLinesLimit) {
                    updateDialogErrorMessage("Reach lines limit in file.\nLimit: $maxLinesLimit.")
                    updateShowDialog(true)
                } else if (urls.isEmpty()) {
                    updateDialogErrorMessage("There is no lines in file")
                    updateShowDialog(true)
                } else if (urls.any { !it.matches(urlPattern) }) {
                    updateDialogErrorMessage("Some of urls have incorrect format")
                    updateShowDialog(true)
                } else {
                    // Add each URL to the list
                    updateUrlList(urls.associateWith { TestCaseParams(false, 0L, 0L, "") })
                }
            } catch (e: IOException) {
                updateDialogErrorMessage("Error reading file: ${e.message}")
                updateShowDialog(true)
            }
        } else {
            updateDialogErrorMessage("File size exceeds the limit of ${maxFileSizeBytes / (1024 * 1024)} MB.\nPlease select another file.")
            updateShowDialog(true)
        }
    } else {
        updateDialogErrorMessage("No file selected.")
        updateShowDialog(true)
    }
}

fun handleAddUrl(
    url: String,
    urlPattern: Regex,
    updateUrlList: (Map<String, TestCaseParams>) -> Unit,
    currentUrls: Map<String, TestCaseParams>,
    resetUrl: () -> Unit,
    updateErrorMessage: (String?) -> Unit
) {
    if (url.matches(urlPattern)) {
        updateUrlList(
            currentUrls + (url to TestCaseParams(
                false,
                0L,
                0L,
                ""
            ))
        ) // TODO check if it is true that we add with 0L
        resetUrl()
        updateErrorMessage(null)
    } else {
        updateErrorMessage("Incorrect URL")
    }
}

fun handleIndividualTimeInputChange(
    input: String,
    key: String,
    updateTime: (Long, String) -> Unit,
    updateUnformattedTime: (String, String) -> Unit,
    updateErrorMessage: (String?) -> Unit
) {
    if (input.isEmpty()) {
        updateTime(0L, key)
        updateUnformattedTime("", key)
        updateErrorMessage(null)
    } else if (
        (input.length == 1 && input.matches(Regex("^([0-9])$"))) ||
        (input.length == 2 && input.matches(Regex("^([0-9][0-9])$"))) ||
        (input.length == 3 && input.matches(Regex("^([0-9][0-9]):$"))) ||
        (input.length == 4 && input.matches(Regex("^([0-9][0-9]):([0-9])$")))
    ) {
        updateTime(0L, key)
        updateUnformattedTime(input, key)
        updateErrorMessage("Invalid format (MM:SS)")
    } else if (input.length == 5 && input.matches(Regex("^([0-9][0-9]):([0-9][0-9])$"))) {
        updateUnformattedTime(input, key)
        updateErrorMessage(null)

        val parts = input.split(":")
        if (parts.size == 2) {
            val minutes = parts[0].toIntOrNull() ?: 0
            val seconds = parts[1].toIntOrNull() ?: 0

            if (seconds in 0..59) {
                updateTime((minutes * 60 + seconds) * 1000L, key)
            } else {
                updateErrorMessage("Seconds must be in the range 00-59")
            }
        }
    } else {
        updateErrorMessage("Invalid format (MM:SS)")
    }

}

fun handleTimeInputChange(
    input: String,
    updateTimeInput: (String) -> Unit,
    updateErrorMessage: (String?) -> Unit,
    updateTimeInMillis: (Long) -> Unit
) {
    if (input.isEmpty()) {
        updateTimeInput("")
        updateTimeInMillis(0L)
        updateErrorMessage(null)
    } else if (
        (input.length == 1 && input.matches(Regex("^([0-9])$"))) ||
        (input.length == 2 && input.matches(Regex("^([0-9][0-9])$"))) ||
        (input.length == 3 && input.matches(Regex("^([0-9][0-9]):$"))) ||
        (input.length == 4 && input.matches(Regex("^([0-9][0-9]):([0-9])$")))
    ) {
        updateTimeInput(input)
        updateTimeInMillis(0L)
        updateErrorMessage("Invalid format (MM:SS)")
    } else if (input.length == 5 && input.matches(Regex("^([0-9][0-9]):([0-9][0-9])$"))) {
        updateTimeInput(input)
        updateErrorMessage(null)

        val parts = input.split(":")
        if (parts.size == 2) {
            val minutes = parts[0].toIntOrNull() ?: 0
            val seconds = parts[1].toIntOrNull() ?: 0

            if (seconds in 0..59) {
                updateTimeInMillis((minutes * 60 + seconds) * 1000L)
            } else {
                updateErrorMessage("Seconds must be in the range 00-59")
            }
        }
    } else {
        updateErrorMessage("Invalid format (MM:SS)")
    }
}

fun handleIndividualTestCountChange(
    input: String,
    key: String,
    updateCount: (Long, String) -> Unit,
    updateErrorMessage: (String?) -> Unit
) {
    val errorMessage = when {
        input.isEmpty() -> {
            updateCount(0L, key)
            null
        }

        input.toLongOrNull() != null -> {
            updateCount(input.toLong(), key)
            null
        }

        else -> {
            "Enter the number"
        }
    }
    updateErrorMessage(errorMessage)
}

fun handleTestCountChange(
    input: String,
    updateCountInput: (String) -> Unit,
    updateRequestCount: (Long) -> Unit,
    updateErrorMessage: (String?) -> Unit
) {
    val errorMessage = when {
        input.isEmpty() -> {
            updateCountInput("")
            updateRequestCount(0L)
            null
        }

        input.toLongOrNull() != null -> {
            updateCountInput(input)
            updateRequestCount(input.toLong())
            null
        }

        else -> {
            "Enter the number"
        }
    }

    updateErrorMessage(errorMessage)
}

fun handleLaunchTest(
    testCase: TestCase,
    isDuration: Boolean,
    cancelFlag: () -> Boolean,
    urlList: Map<String, TestCaseParams>,
    durationErrorMessage: String?,
    coroutineScope: CoroutineScope,
    onUpdateTestCase: (TestCase) -> Unit,
    updateProgress: (Long) -> Unit,
    updateShowDialog: (Boolean) -> Unit,
    updateDialogMessage: (String) -> Unit
) {
    val missingFields = mutableListOf<String>()
    if (urlList.isEmpty()) {
        missingFields.add("URLs list is empty!")
    }
    if (!durationErrorMessage.isNullOrEmpty()) {
        missingFields.add("Duration format is incorrect!")
    }
    if (isDuration && urlList.values.any { it.durationValue == 0L }) {
        missingFields.add(
            "Time duration doesn't set!\n" +
                    urlList.filter { it.value.durationValue == 0L }
                        .keys
                        .joinToString(", "))
    }
    if (!isDuration && urlList.values.any { it.countValue == 0L }) {
        missingFields.add(
            "Requests count doesn't set!\n" +
                urlList.filter { it.value.countValue == 0L }
                    .keys
                    .joinToString(", "))
    }

    if (missingFields.isNotEmpty()) {
        val errorMessage = "Incorrect values for running the test:\n" + missingFields.joinToString("\n")
        updateDialogMessage(errorMessage)
        updateShowDialog(true)
    } else {
        val tmpTestCase = TestCase(
            testCase.okHttpClient,
            urlList,
            if (isDuration) RunType.DURATION else RunType.COUNT,
        )
        onUpdateTestCase(tmpTestCase)

        coroutineScope.launch {
            launch {
                tmpTestCase.runTestCase(cancelFlag)
            }
            launch {
                updateProgress(0)
                while (tmpTestCase.testCaseState.getStatus() == StatusCode.RUNNING ||
                    tmpTestCase.testCaseState.getStatus() == StatusCode.CREATED
                ) {
                    delay(1000)
                    updateProgress(tmpTestCase.getProgress())
                }
            }
        }
    }
}