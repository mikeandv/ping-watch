package com.github.mikeandv.pingwatch.ui.viewmodels

import com.github.mikeandv.pingwatch.RunType
import com.github.mikeandv.pingwatch.entity.TestCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainScreenViewModel {

    private val _urlList = MutableStateFlow<Set<String>>(emptySet())
    val urlList: StateFlow<Set<String>> = _urlList

    private val _testCase = MutableStateFlow(TestCase(listOf(), RunType.COUNT, 0, 0))
    val testCase: StateFlow<TestCase> = _testCase

    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress

    private val _urlErrorMessage = MutableStateFlow<String?>(null)
    val urlErrorMessage: StateFlow<String?> = _urlErrorMessage

    private val _durationErrorMessage = MutableStateFlow<String?>(null)
    val durationErrorMessage: StateFlow<String?> = _durationErrorMessage

    private val _dialogErrorMessage = MutableStateFlow<String?>(null)
    val dialogErrorMessage: StateFlow<String?> = _dialogErrorMessage

    private val _timeInMillis = MutableStateFlow<Long?>(null)
    val timeInMillis: StateFlow<Long?> = _timeInMillis

    private val _requestCount = MutableStateFlow<Long?>(null)
    val requestCount: StateFlow<Long?> = _requestCount

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog


    fun updateUrlList(newUrls: Set<String>) {
        _urlList.value = newUrls
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

    fun updateTimeInMillis(newTimeInMillis: Long?) {
        _timeInMillis.value = newTimeInMillis
    }

    fun updateRequestCount(newRequestCount: Long?) {
        _requestCount.value = newRequestCount
    }

    fun updateShowDialog(newShowDialog: Boolean) {
        _showDialog.value = newShowDialog
    }
}

