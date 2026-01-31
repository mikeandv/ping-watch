package com.github.mikeandv.pingwatch.ui.viewmodels

import com.github.mikeandv.pingwatch.domain.*
import com.github.mikeandv.pingwatch.utils.convertMillisToTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainScreenViewModel {

    private val _testCase = MutableStateFlow(
        TestCase(
            urls = emptyMap(),
            runType = RunType.COUNT,
            executionMode = ExecutionMode.SEQUENTIAL,
            parallelism = 8,
            settings = TestCaseSettings()
        )
    )
    val testCase: StateFlow<TestCase> = _testCase

    private val _parallelismInput = MutableStateFlow("8")
    val parallelismInput: StateFlow<String> = _parallelismInput

    private val _parallelismError = MutableStateFlow<String?>(null)
    val parallelismError: StateFlow<String?> = _parallelismError

    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress

    private val _urlErrorMessage = MutableStateFlow<String?>(null)
    val urlErrorMessage: StateFlow<String?> = _urlErrorMessage

    private val _durationErrorMessage = MutableStateFlow<String?>(null)
    val durationErrorMessage: StateFlow<String?> = _durationErrorMessage

    private val _dialogErrorMessage = MutableStateFlow<String?>(null)
    val dialogErrorMessage: StateFlow<String?> = _dialogErrorMessage

    private val _countInput = MutableStateFlow("")
    val countInput: StateFlow<String> = _countInput

    private val _timeInput = MutableStateFlow("")
    val timeInput: StateFlow<String> = _timeInput

    private val _tags = MutableStateFlow(emptyList<Category>())
    val tags: StateFlow<List<Category>> = _tags

    private val _individualErrorMsg = MutableStateFlow<Map<String, String?>>(emptyMap())
    val individualErrorMsg: StateFlow<Map<String, String?>> = _individualErrorMsg

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog

    private var _requestCount = 0L

    private var _timeMillis = 0L

    fun updateUrlList(newUrls: Map<String, TestCaseParams>) {
        _testCase.value = _testCase.value.copy(urls = newUrls)
    }

    fun updateUrlList(newUrlList: List<String>) {
        val newEntries = newUrlList.associateWith {
            TestCaseParams(false, _requestCount, _timeMillis, "", null)
        }
        val oldUrls = _testCase.value.urls
        _testCase.value = _testCase.value.copy(urls = oldUrls + newEntries)
    }

    fun updateTestCase(newTestCase: TestCase) {
        _testCase.value = newTestCase
    }

    fun updateProgress(newProgress: Long) {
        _progress.value = newProgress
    }

    fun updateUrlErrorMessage(newMessage: String?) {
        _urlErrorMessage.value = newMessage
    }

    fun updateDurationErrorMessage(newMessage: String?) {
        _durationErrorMessage.value = newMessage
    }

    fun updateDialogErrorMessage(newMessage: String?) {
        _dialogErrorMessage.value = newMessage
    }

    fun updateTimeInMillis(newTimeInMillis: Long) {
        _timeMillis = newTimeInMillis
        val updatedUrls = _testCase.value.urls.mapValues { (_, value) ->
            if (value.isEdit) {
                value
            } else {
                TestCaseParams(
                    false,
                    value.countValue,
                    newTimeInMillis,
                    convertMillisToTime(newTimeInMillis),
                    value.tag
                )
            }
        }
        _testCase.value = _testCase.value.copy(urls = updatedUrls)
    }

    fun updateRequestCount(newRequestCount: Long) {
        _requestCount = newRequestCount
        val updatedUrls = _testCase.value.urls.mapValues { (_, value) ->
            if (value.isEdit) {
                value
            } else {
                TestCaseParams(false, newRequestCount, value.durationValue, value.unformattedDurationValue, value.tag)
            }
        }
        _testCase.value = _testCase.value.copy(urls = updatedUrls)
    }

    fun updateRequestCountByKey(newRequestCount: Long, key: String) {
        val updatedMap = _testCase.value.urls.toMutableMap()
        updatedMap[key]?.let { oldValue ->
            updatedMap[key] =
                TestCaseParams(
                    oldValue.isEdit,
                    newRequestCount,
                    oldValue.durationValue,
                    oldValue.unformattedDurationValue,
                    oldValue.tag
                )
        }
        _testCase.value = _testCase.value.copy(urls = updatedMap)
    }

    fun updateTimeInMillisByKey(newTimeInMillis: Long, key: String) {
        val updatedMap = _testCase.value.urls.toMutableMap()
        updatedMap[key]?.let { oldValue ->
            updatedMap[key] =
                TestCaseParams(
                    oldValue.isEdit,
                    oldValue.countValue,
                    newTimeInMillis,
                    convertMillisToTime(newTimeInMillis),
                    oldValue.tag
                )
        }
        _testCase.value = _testCase.value.copy(urls = updatedMap)
    }

    fun updateUnformattedDurationValueByKey(newUnformattedDurationValue: String, key: String) {
        val updatedMap = _testCase.value.urls.toMutableMap()
        updatedMap[key]?.let { oldValue ->
            updatedMap[key] =
                TestCaseParams(
                    oldValue.isEdit,
                    oldValue.countValue,
                    oldValue.durationValue,
                    newUnformattedDurationValue,
                    oldValue.tag
                )
        }
        _testCase.value = _testCase.value.copy(urls = updatedMap)
    }

    fun updateIsEditByKey(newIsEdit: Boolean, key: String) {
        val updatedMap = _testCase.value.urls.toMutableMap()
        updatedMap[key]?.let { oldValue ->
            updatedMap[key] = TestCaseParams(
                newIsEdit,
                oldValue.countValue,
                oldValue.durationValue,
                oldValue.unformattedDurationValue,
                oldValue.tag
            )
        }
        _testCase.value = _testCase.value.copy(urls = updatedMap)
    }

    fun updateTagByKey(newTag: Category?, key: String) {
        val updatedMap = _testCase.value.urls.toMutableMap()
        updatedMap[key]?.let { oldValue ->
            updatedMap[key] = TestCaseParams(
                oldValue.isEdit,
                oldValue.countValue,
                oldValue.durationValue,
                oldValue.unformattedDurationValue,
                newTag
            )
        }
        _testCase.value = _testCase.value.copy(urls = updatedMap)
    }

    fun updateIsDuration(newIsDuration: Boolean) {
        if (newIsDuration) {
            _testCase.value = _testCase.value.copy(runType = RunType.DURATION)
        } else {
            _testCase.value = _testCase.value.copy(runType = RunType.COUNT)
        }
    }

    fun updateCountInput(newCountInput: String) {
        _countInput.value = newCountInput
    }

    fun updateTimeInput(newTimeInput: String) {
        _timeInput.value = newTimeInput
    }

    fun updateShowDialog(newShowDialog: Boolean) {
        _showDialog.value = newShowDialog
    }

    fun updateTestCaseSettings(newTestCaseSettings: TestCaseSettings) {
        _testCase.value = _testCase.value.copy(settings = newTestCaseSettings)
    }

    fun updateExecutionMode(newExecutionMode: ExecutionMode) {
        _testCase.value = _testCase.value.copy(executionMode = newExecutionMode)
    }

    fun updateParallelismInput(newParallelismInput: String) {
        _parallelismInput.value = newParallelismInput
    }

    fun updateParallelismError(newParallelismError: String?) {
        _parallelismError.value = newParallelismError
    }

    fun updateParallelism(newParallelism: Int) {
        _testCase.value = _testCase.value.copy(parallelism = newParallelism)
    }

    fun addTag(tag: Category) {
        _tags.value += tag
    }

    fun updateIndividualErrorMsg(url: String, newErrorMsg: String?, ) {
        val updatedMap = _individualErrorMsg.value.toMutableMap()
        updatedMap[url] = newErrorMsg
        _individualErrorMsg.value = updatedMap
    }
}

