package com.github.mikeandv.pingwatch.ui.viewmodels

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.convertMillisToTime
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseParams
import com.github.mikeandv.pingwatch.entity.TestCaseSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainScreenViewModel {

    private val _testCaseSettings = MutableStateFlow(TestCaseSettings.createDefaultSettings())
    val testCaseSettings: StateFlow<TestCaseSettings> = _testCaseSettings

    private val _urlList = MutableStateFlow(emptyMap<String, TestCaseParams>())
    val urlList: StateFlow<Map<String, TestCaseParams>> = _urlList

    private val _testCase = MutableStateFlow(TestCase(emptyMap(), RunType.COUNT, testCaseSettings.value))
    val testCase: StateFlow<TestCase> = _testCase

    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress

    private val _urlErrorMessage = MutableStateFlow<String?>(null)
    val urlErrorMessage: StateFlow<String?> = _urlErrorMessage

    private val _durationErrorMessage = MutableStateFlow<String?>(null)
    val durationErrorMessage: StateFlow<String?> = _durationErrorMessage

    private val _dialogErrorMessage = MutableStateFlow<String?>(null)
    val dialogErrorMessage: StateFlow<String?> = _dialogErrorMessage

    private val _isDuration = MutableStateFlow(true)
    val isDuration: StateFlow<Boolean> = _isDuration

    private val _countInput = MutableStateFlow("")
    val countInput: StateFlow<String> = _countInput

    private val _timeInput = MutableStateFlow("")
    val timeInput: StateFlow<String> = _timeInput

    private val _requestCount = MutableStateFlow(0L)
    val requestCount: StateFlow<Long> = _requestCount

    private val _timeMillis = MutableStateFlow(0L)
    val timeMillis: StateFlow<Long> = _timeMillis


    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog


    fun updateUrlList(newUrls: Map<String, TestCaseParams>) {
        _urlList.value = newUrls
    }

    fun updateUrlList(newUrlList: List<String>) {
        val newEntries = newUrlList.associateWith {
            TestCaseParams(false, _requestCount.value, _timeMillis.value, "")
        }
        _urlList.value += newEntries
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
        _timeMillis.value = newTimeInMillis
        _urlList.value = _urlList.value.mapValues { (_, value) ->
            if (value.isEdit) {
                value
            } else {
                TestCaseParams(false, value.countValue, newTimeInMillis, convertMillisToTime(newTimeInMillis))
            }
        }
    }

    fun updateRequestCount(newRequestCount: Long) {
        _requestCount.value = newRequestCount
        _urlList.value = _urlList.value.mapValues { (_, value) ->
            if (value.isEdit) {
                value
            } else {
                TestCaseParams(false, newRequestCount, value.durationValue, value.unformattedDurationValue)
            }
        }
    }

    fun updateRequestCountByKey(newRequestCount: Long, key: String) {
        val updatedMap = _urlList.value.toMutableMap()
        updatedMap[key]?.let { oldValue ->
            updatedMap[key] =
                TestCaseParams(oldValue.isEdit, newRequestCount, oldValue.durationValue, oldValue.unformattedDurationValue)
        }
        _urlList.value = updatedMap
    }

    fun updateTimeInMillisByKey(newTimeInMillis: Long, key: String) {
        val updatedMap = _urlList.value.toMutableMap()
        updatedMap[key]?.let { oldValue ->
            updatedMap[key] =
                TestCaseParams(oldValue.isEdit, oldValue.countValue, newTimeInMillis, convertMillisToTime(newTimeInMillis))
        }
        _urlList.value = updatedMap
    }

    fun updateUnformattedDurationValueByKey(newUnformattedDurationValue: String, key: String) {
        val updatedMap = _urlList.value.toMutableMap()
        updatedMap[key]?.let { oldValue ->
            updatedMap[key] =
                TestCaseParams(oldValue.isEdit, oldValue.countValue, oldValue.durationValue, newUnformattedDurationValue)
        }
        _urlList.value = updatedMap
    }

    fun updateIsEditByKey(newIsEdit: Boolean, key: String) {
        val updatedMap = _urlList.value.toMutableMap()
        updatedMap[key]?.let { oldValue ->
            updatedMap[key] = TestCaseParams(
                newIsEdit,
                oldValue.countValue,
                oldValue.durationValue,
                oldValue.unformattedDurationValue
            )
        }
        _urlList.value = updatedMap
    }

    fun updateIsDuration(newIsDuration: Boolean) {
        _isDuration.value = newIsDuration
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
        _testCaseSettings.value = newTestCaseSettings
    }
}

