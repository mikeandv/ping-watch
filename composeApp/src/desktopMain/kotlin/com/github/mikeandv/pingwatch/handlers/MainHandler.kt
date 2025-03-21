package com.github.mikeandv.pingwatch.handlers

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.StatusCode
import com.github.mikeandv.pingwatch.entity.TestCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import java.io.IOException

fun handleUrlChange(
    input: String,
    updateUrl: (String) -> Unit,
    updateErrorMessage: (String?) -> Unit,
    urlPattern: Regex
) {
    updateUrl(input)
    updateErrorMessage(
        if (input.matches(urlPattern) || input.isEmpty()) null else "URL must start with http:// or https://"
    )
}

fun handleImport(
    updateUrlList: (Set<String>) -> Unit,
    updateShowDialog: (Boolean) -> Unit,
    updateDialogErrorMessage: (String?) -> Unit
) {
    val dialog = FileDialog(Frame(), "Select File", FileDialog.LOAD)
    dialog.isVisible = true
    val selectedFile = dialog.files.firstOrNull()

    if (selectedFile != null) {
        try {
            val urls = selectedFile.readLines() // Read the file as lines
            // Add each URL to the list
            updateUrlList(urls.toSet())
        } catch (e: IOException) {
            updateDialogErrorMessage("Error reading file: ${e.message}")
            updateShowDialog(true)
        }
    }
}

fun handleAddUrl(
    url: String,
    urlPattern: Regex,
    updateUrlList: (Set<String>) -> Unit,
    currentUrls: Set<String>,
    resetUrl: () -> Unit,
    updateErrorMessage: (String?) -> Unit
) {
    if (url.matches(urlPattern)) {
        updateUrlList(currentUrls + url)
        resetUrl()
        updateErrorMessage(null)
    } else {
        updateErrorMessage("Incorrect URL")
    }
}

fun handleTimeInputChange(
    input: String,
    updateTimeInput: (String) -> Unit,
    updateErrorMessage: (String?) -> Unit,
    updateTimeInMillis: (Long?) -> Unit
) {
    if (input.isEmpty()) {
        updateTimeInput("")
        updateTimeInMillis(null)
        updateErrorMessage(null)
    } else if (input.matches(Regex("^\\d{0,2}:?\\d{0,2}$"))) { // Check MM:SS format
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

fun handleTestCountChange(
    input: String,
    updateCountInput: (String) -> Unit,
    updateRequestCount: (Long?) -> Unit,
    updateErrorMessage: (String?) -> Unit
) {
    val errorMessage = when {
        input.isEmpty() -> {
            updateCountInput("")
            updateRequestCount(null)
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
    isDuration: Boolean,
    urlList: Set<String>,
    requestCount: Long?,
    timeInMillis: Long?,
    durationErrorMessage: String?,
    coroutineScope: CoroutineScope,
    onUpdateTestCase: (TestCase) -> Unit,
    updateProgress: (Long) -> Unit,
    updateShowDialog: (Boolean) -> Unit,
    updateDialogMessage: (String) -> Unit
) {
    if (urlList.isEmpty() || !durationErrorMessage.isNullOrEmpty() ||
        (isDuration && timeInMillis == null) ||
        (!isDuration && requestCount == null)
    ) {
        updateDialogMessage("Incorrect values for running the test!\n")
        updateShowDialog(true)
    } else {
        val tmpTestCase = TestCase(
            urlList.toList(),
            if (isDuration) RunType.DURATION else RunType.COUNT,
            if (isDuration) 0 else requestCount,
            if (isDuration) timeInMillis else 0
        )
        onUpdateTestCase(tmpTestCase)

        coroutineScope.launch {
            launch {
                tmpTestCase.runTestCase()
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