package com.github.mikeandv.pingwatch.handlers

import com.github.mikeandv.pingwatch.entity.CountInputResult
import com.github.mikeandv.pingwatch.entity.ParallelismInputResult
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseParams
import com.github.mikeandv.pingwatch.entity.TimeInputResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun normalizeUrl(url: String): String? {
    return url.toHttpUrlOrNull()?.toString()
}

//private val okHttpClient = OkHttpClient()

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
    updateUrlList: (List<String>) -> Unit,
    updateShowDialog: (Boolean) -> Unit,
    updateDialogErrorMessage: (String?) -> Unit,
    urlPattern: Regex
) {
    val maxFileSizeBytes = 5 * 1024 * 1024 // file size 5mb limit
    val maxLinesLimit = 20  // limit to lines in file


    val fileChooser = JFileChooser()
    val filter = FileNameExtensionFilter("Text File (*.txt)", "txt")
    fileChooser.fileFilter = filter


    if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
        updateDialogErrorMessage("No file selected.")
        updateShowDialog(true)
        return
    }

    val file = fileChooser.selectedFile

    if (file.length() > maxFileSizeBytes) {
        updateDialogErrorMessage("File size exceeds the limit of ${maxFileSizeBytes / (1024 * 1024)} MB.\nPlease select another file.")
        updateShowDialog(true)
        return
    }

    val result = runCatching {
        validateUrlsFile(file.readLines(), maxLinesLimit, urlPattern)
    }.getOrElse {
        updateDialogErrorMessage("Error reading file: ${it.message}")
        updateShowDialog(true)
        return
    }

    result
        .onSuccess { updateUrlList(it) }
        .onFailure {
            updateDialogErrorMessage(it.message)
            updateShowDialog(true)
        }
}

fun handleAddUrl(
    url: String,
    urlPattern: Regex,
    updateUrlList: (List<String>) -> Unit,
    resetUrl: () -> Unit,
    updateErrorMessage: (String?) -> Unit
) {
    if (!url.matches(urlPattern)) {
        updateErrorMessage("Incorrect URL format")
        return
    }

    val normalizedUrl = normalizeUrl(url)
    if (normalizedUrl == null) {
        updateErrorMessage("URL cannot be normalized")
        return
    }

    updateUrlList(listOf(normalizedUrl))
    resetUrl()
    updateErrorMessage(null)
}

fun handleIndividualTimeInputChange(
    input: String,
    key: String,
    updateTime: (Long, String) -> Unit,
    updateUnformattedTime: (String, String) -> Unit,
    updateErrorMessage: (String?) -> Unit
) {
    when (val result = processTimeInput(input)) {
        is TimeInputResult.Empty -> {
            updateTime(result.timeMillis, key)
            updateUnformattedTime(result.unformatted, key)
            updateErrorMessage(null)
        }

        is TimeInputResult.Partial -> {
            updateTime(0L, key)
            updateUnformattedTime(result.unformatted, key)
            updateErrorMessage(result.error)
        }

        is TimeInputResult.Valid -> {
            updateUnformattedTime(result.unformatted, key)
            updateTime(result.timeMillis, key)
            updateErrorMessage(null)
        }

        is TimeInputResult.Error -> {
            updateErrorMessage(result.message)
        }
    }
}

fun handleTimeInputChange(
    input: String,
    updateTimeInput: (String) -> Unit,
    updateErrorMessage: (String?) -> Unit,
    updateTimeInMillis: (Long) -> Unit
) {
    when (val result = processTimeInput(input)) {
        is TimeInputResult.Empty -> {
            updateTimeInput(result.unformatted)
            updateTimeInMillis(result.timeMillis)
            updateErrorMessage(null)
        }

        is TimeInputResult.Partial -> {
            updateTimeInput(result.unformatted)
            updateTimeInMillis(0L)
            updateErrorMessage(result.error)
        }

        is TimeInputResult.Valid -> {
            updateTimeInput(result.unformatted)
            updateTimeInMillis(result.timeMillis)
            updateErrorMessage(null)
        }

        is TimeInputResult.Error -> {
            updateErrorMessage(result.message)
        }
    }
}


fun handleIndividualTestCountChange(
    input: String,
    key: String,
    updateCount: (Long, String) -> Unit,
    updateErrorMessage: (String?) -> Unit
) {
    when (val result = processCountInput(input)) {
        is CountInputResult.Empty -> {
            updateCount(result.value, key)
            updateErrorMessage(null)
        }

        is CountInputResult.Valid -> {
            updateCount(result.value, key)
            updateErrorMessage(null)
        }

        is CountInputResult.Error -> {
            updateErrorMessage(result.message)
        }
    }
}


fun handleTestCountChange(
    input: String,
    updateCountInput: (String) -> Unit,
    updateRequestCount: (Long) -> Unit,
    updateErrorMessage: (String?) -> Unit
) {
    when (val result = processCountInput(input)) {
        is CountInputResult.Empty -> {
            updateCountInput("")
            updateRequestCount(result.value)
            updateErrorMessage(null)
        }

        is CountInputResult.Valid -> {
            updateCountInput(input)
            updateRequestCount(result.value)
            updateErrorMessage(null)
        }

        is CountInputResult.Error -> {
            updateErrorMessage(result.message)
        }
    }
}

fun handleParallelismInputChange(
    input: String,
    updateParallelismInput: (String) -> Unit,
    updateErrorMessage: (String?) -> Unit
) {
    updateParallelismInput(input)
    when (val result = processParallelismInput(input)) {
        is ParallelismInputResult.Valid -> {
            updateErrorMessage(null)
        }
        is ParallelismInputResult.Error -> {
            updateErrorMessage(result.message)
        }
    }
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
    val validation = validateLaunchTest(urlList, isDuration, durationErrorMessage)

    if (!validation.isValid) {
        updateDialogMessage(validation.errorMessage!!)
        updateShowDialog(true)
        return
    }

    val tmpTestCase = buildTestCase(testCase, urlList, isDuration)
    onUpdateTestCase(tmpTestCase)

    coroutineScope.launch {
        tmpTestCase.progressFlow()
            .collect { updateProgress(it.toLong()) }
    }

    coroutineScope.launch {
        tmpTestCase.runTestCase(cancelFlag)
    }
}
