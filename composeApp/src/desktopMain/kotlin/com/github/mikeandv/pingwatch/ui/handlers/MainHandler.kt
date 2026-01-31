package com.github.mikeandv.pingwatch.ui.handlers

import com.github.mikeandv.pingwatch.domain.TestCase
import com.github.mikeandv.pingwatch.ui.utils.CountInputResult
import com.github.mikeandv.pingwatch.ui.utils.IntInputResult
import com.github.mikeandv.pingwatch.ui.utils.ParallelismInputResult
import com.github.mikeandv.pingwatch.ui.utils.TimeInputResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun normalizeUrl(url: String): String? {
    return url.toHttpUrlOrNull()?.toString()
}

private const val mbMultiplicator = 1024 * 1024

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
    urlPattern: Regex,
    maxFileSize: Int,
    maxLinesLimit: Int,
    allowedFileExtensions: List<String>
) {
    val maxFileSizeBytes = maxFileSize * mbMultiplicator

    val fileChooser = JFileChooser()
    val extensionsDisplay = allowedFileExtensions.joinToString(", ") { "*.$it" }
    val filter = FileNameExtensionFilter("Files ($extensionsDisplay)", *allowedFileExtensions.toTypedArray())
    fileChooser.fileFilter = filter


    if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
        updateDialogErrorMessage("No file selected.")
        updateShowDialog(true)
        return
    }

    val file = fileChooser.selectedFile

    if (file.extension !in allowedFileExtensions) {
        updateDialogErrorMessage("Allowed file extension is ${allowedFileExtensions.joinToString(", ")}")
        updateShowDialog(true)
        return
    }

    if (file.length() > maxFileSizeBytes) {
        updateDialogErrorMessage("File size exceeds the limit of ${maxFileSizeBytes / (mbMultiplicator)} MB.\nPlease select another file.")
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
    updateErrorMessage: (String, String?) -> Unit
) {
    when (val result = processTimeInput(input)) {
        is TimeInputResult.Empty -> {
            updateTime(result.timeMillis, key)
            updateUnformattedTime(result.unformatted, key)
            updateErrorMessage(key, null)
        }

        is TimeInputResult.Partial -> {
            updateTime(0L, key)
            updateUnformattedTime(result.unformatted, key)
            updateErrorMessage(key, result.error)
        }

        is TimeInputResult.Valid -> {
            updateUnformattedTime(result.unformatted, key)
            updateTime(result.timeMillis, key)
            updateErrorMessage(key, null)
        }

        is TimeInputResult.Error -> {
            updateErrorMessage(key, result.message)
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
    updateErrorMessage: (String, String?) -> Unit,
    fieldMinLimit: Int,
    fieldMaxLimit: Int
) {
    when (val result = processCountInput(input, fieldMinLimit, fieldMaxLimit)) {
        is CountInputResult.Empty -> {
            updateCount(result.value, key)
            updateErrorMessage(key, null)
        }

        is CountInputResult.Valid -> {
            updateCount(result.value, key)
            updateErrorMessage(key, null)
        }

        is CountInputResult.Error -> {
            updateErrorMessage(key, result.message)
        }
    }
}


fun handleTestCountChange(
    input: String,
    updateCountInput: (String) -> Unit,
    updateRequestCount: (Long) -> Unit,
    updateErrorMessage: (String?) -> Unit,
    fieldMinLimit: Int,
    fieldMaxLimit: Int
) {
    when (val result = processCountInput(input, fieldMinLimit, fieldMaxLimit)) {
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
    updateParallelism: (Int) -> Unit,
    updateErrorMessage: (String?) -> Unit,
    fieldMinLimit: Int,
    fieldMaxLimit: Int
) {

    when (val result = processParallelismInput(input, fieldMinLimit, fieldMaxLimit)) {
        is ParallelismInputResult.Empty -> {
            updateParallelismInput("")
            updateParallelism(result.value)
            updateErrorMessage(null)
        }

        is ParallelismInputResult.Valid -> {
            updateParallelismInput(input)
            updateParallelism(result.value)
            updateErrorMessage(null)
        }

        is ParallelismInputResult.Error -> {
            updateErrorMessage(result.message)
        }
    }
}

fun handleIntInputChange(
    input: String,
    updateInput: (String) -> Unit,
    updateErrorMessage: (String?) -> Unit,
    fieldMinLimit: Int,
    fieldMaxLimit: Int
) {
    updateInput(input)
    when (val result = processIntInput(input, fieldMinLimit, fieldMaxLimit)) {
        is IntInputResult.Valid -> updateErrorMessage(null)
        is IntInputResult.Error -> updateErrorMessage(result.message)
    }
}

fun handleLaunchTest(
    testCase: TestCase,
    cancelFlag: () -> Boolean,
    resetCancelFlag: () -> Unit,
    durationErrorMessage: String?,
    parallelismError: String?,
    coroutineScope: CoroutineScope,
    updateProgress: (Long) -> Unit,
    updateShowDialog: (Boolean) -> Unit,
    updateDialogMessage: (String) -> Unit
) {
    val validation = validateLaunchTest(
        testCase,
        durationErrorMessage,
        parallelismError
    )

    if (!validation.isValid) {
        updateDialogMessage(validation.errorMessage!!)
        updateShowDialog(true)
        return
    }

    resetCancelFlag()

    coroutineScope.launch {
        testCase.progressFlow()
            .collect { updateProgress(it.toLong()) }
    }

    coroutineScope.launch {
        testCase.runTestCase(cancelFlag)
    }
}
