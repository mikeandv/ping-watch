package com.github.mikeandv.pingwatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.ui.components.*
import com.github.mikeandv.pingwatch.ui.handlers.*
import com.github.mikeandv.pingwatch.ui.viewmodels.MainScreenViewModel
import java.util.concurrent.atomic.AtomicBoolean

private val URL_PATTERN = Regex("^https?://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/\\S*)?$")

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    onNavigate: () -> Unit
) {
    val testCase by viewModel.testCase.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val urlErrorMessage by viewModel.urlErrorMessage.collectAsState()
    val durationErrorMessage by viewModel.durationErrorMessage.collectAsState()
    val dialogErrorMessage by viewModel.dialogErrorMessage.collectAsState()
    val countInput by viewModel.countInput.collectAsState()
    val timeInput by viewModel.timeInput.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val parallelismInput by viewModel.parallelismInput.collectAsState()
    val parallelismError by viewModel.parallelismError.collectAsState()
    val individualErrorMsgMap by viewModel.individualErrorMsg.collectAsState()

    var url by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val cancelFlag = remember { AtomicBoolean(false) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                FlowControlButtons(
                    testCaseState = testCase.testCaseState,
                    onLaunchTest = {
                        handleLaunchTest(
                            testCase,
                            { cancelFlag.get() },
                            { cancelFlag.set(false) },
                            durationErrorMessage,
                            parallelismError,
                            coroutineScope,
                            viewModel::updateProgress,
                            viewModel::updateShowDialog,
                            viewModel::updateDialogErrorMessage
                        )
                    },
                    onNavigate = onNavigate,
                    cancelFlag = cancelFlag,
                    updateShowDialog = viewModel::updateShowDialog,
                    updateDialogMessage = viewModel::updateDialogErrorMessage,
                    onSettingsClick = { showSettingsDialog = true }
                )
            }

            ParametersSection(
                testCase = testCase,
                url = url,
                onUrlChange = { input ->
                    handleUrlChange(input, { url = it }, viewModel::updateUrlErrorMessage, URL_PATTERN)
                },
                urlErrorMessage = urlErrorMessage,
                onAddUrl = {
                    handleAddUrl(
                        url,
                        URL_PATTERN,
                        viewModel::updateUrlList,
                        { url = "" },
                        viewModel::updateUrlErrorMessage
                    )
                },
                onImport = {
                    handleImport(
                        viewModel::updateUrlList,
                        viewModel::updateShowDialog,
                        viewModel::updateDialogErrorMessage,
                        URL_PATTERN,
                        testCase.settings.maxFileSize,
                        testCase.settings.maxLinesLimit,
                        testCase.settings.allowedFileExtensions
                    )
                },
                onDurationSelected = {
                    viewModel.updateIsDuration(true)
                    viewModel.updateDurationErrorMessage(null)
                },
                onCountSelected = {
                    viewModel.updateIsDuration(false)
                    viewModel.updateDurationErrorMessage(null)
                },
                countInput = countInput,
                timeInput = timeInput,
                durationErrorMessage = durationErrorMessage,
                onTimeInputChange = { input ->
                    handleTimeInputChange(
                        input,
                        viewModel::updateTimeInput,
                        viewModel::updateDurationErrorMessage,
                        viewModel::updateTimeInMillis
                    )
                },
                onCountInputChange = { input ->
                    handleTestCountChange(
                        input,
                        viewModel::updateCountInput,
                        viewModel::updateRequestCount,
                        viewModel::updateDurationErrorMessage,
                        testCase.settings.minCommonInput,
                        testCase.settings.maxCountInput
                    )
                },
                progress = progress,
                onExecutionModeChange = { mode ->
                    viewModel.updateParallelismError(null)
                    viewModel.updateExecutionMode(mode)
                },
                parallelismInput = parallelismInput,
                parallelismError = parallelismError,
                onParallelismChange = { input ->
                    handleParallelismInputChange(
                        input,
                        viewModel::updateParallelismInput,
                        viewModel::updateParallelism,
                        viewModel::updateParallelismError,
                        testCase.settings.minCommonInput,
                        testCase.settings.maxParallelismInput
                    )
                }
            )

            UrlListSection(
                testCase = testCase,
                modifier = Modifier.weight(4f),
                timeInput = timeInput,
                countInput = countInput,
                updateIndividualCount = viewModel::updateRequestCountByKey,
                updateIndividualTime = viewModel::updateTimeInMillisByKey,
                updateIndividualUnformattedTime = viewModel::updateUnformattedDurationValueByKey,
                updateIndividualIsEdit = viewModel::updateIsEditByKey,
                onRemoveUrl = { viewModel.updateUrlList(testCase.urls.minus(it)) },
                onIndividualErrorChange = viewModel::updateIndividualErrorMsg,
                individualErrorMsgMap = individualErrorMsgMap
            )
        }

        ErrorDialog(
            showDialog = showDialog,
            message = dialogErrorMessage,
            onDismiss = { viewModel.updateShowDialog(false) }
        )

        SettingsDialog(
            showDialog = showSettingsDialog,
            settings = testCase.settings,
            onDismiss = { showSettingsDialog = false },
            onSave = { newSettings ->
                viewModel.updateTestCaseSettings(newSettings)
                testCase.updateSettings(newSettings)
                showSettingsDialog = false
            }
        )
    }
}
