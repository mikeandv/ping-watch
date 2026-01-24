package com.github.mikeandv.pingwatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.handlers.*
import com.github.mikeandv.pingwatch.ui.components.*
import com.github.mikeandv.pingwatch.ui.viewmodels.MainScreenViewModel
import java.util.concurrent.atomic.AtomicBoolean

private val URL_PATTERN = Regex("^https?://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/\\S*)?$")

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    onNavigate: () -> Unit
) {
    val testCaseSettings by viewModel.testCaseSettings.collectAsState()
    val testCase by viewModel.testCase.collectAsState()
    val urlList by viewModel.urlList.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val urlErrorMessage by viewModel.urlErrorMessage.collectAsState()
    val durationErrorMessage by viewModel.durationErrorMessage.collectAsState()
    val dialogErrorMessage by viewModel.dialogErrorMessage.collectAsState()
    val isDuration by viewModel.isDuration.collectAsState()
    val countInput by viewModel.countInput.collectAsState()
    val timeInput by viewModel.timeInput.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val executionMode by viewModel.executionMode.collectAsState()
    val parallelismInput by viewModel.parallelismInput.collectAsState()
    val parallelismError by viewModel.parallelismError.collectAsState()
    val parallelism by viewModel.parallelism.collectAsState()

    var url by remember { mutableStateOf("") }
    var individualErrorMessage by remember { mutableStateOf<String?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val cancelFlag = remember { AtomicBoolean(false) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val errorMessage = urlErrorMessage ?: durationErrorMessage ?: parallelismError ?: individualErrorMessage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(
                        if (errorMessage != null) Color(0xFFFFD54F) else Color.Transparent,
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = Color.Black,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                FlowControlButtons(
                    testCase = testCase,
                    onLaunchTest = {
                        handleLaunchTest(
                            testCase, isDuration, executionMode, parallelism,
                            { cancelFlag.get() }, urlList, durationErrorMessage, parallelismError,
                            coroutineScope, viewModel::updateTestCase, viewModel::updateProgress,
                            viewModel::updateShowDialog, viewModel::updateDialogErrorMessage
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
                        testCaseSettings.maxFileSize,
                        testCaseSettings.maxLinesLimit,
                        testCaseSettings.allowedFileExtensions
                    )
                },
                isDuration = isDuration,
                onDurationSelected = {
                    viewModel.updateIsDuration(true)
                    viewModel.updateDurationErrorMessage(null)
                    viewModel.updateCountInput("")
                },
                onCountSelected = {
                    viewModel.updateIsDuration(false)
                    viewModel.updateDurationErrorMessage(null)
                    viewModel.updateTimeInput("")
                },
                countInput = countInput,
                timeInput = timeInput,
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
                        viewModel::updateDurationErrorMessage
                    )
                },
                progress = progress,
                isDurationCountEnabled = urlList.isNotEmpty(),
                executionMode = executionMode,
                onExecutionModeChange = { mode ->
                    viewModel.updateExecutionMode(mode)
                },
                parallelismInput = parallelismInput,
                parallelismError = parallelismError,
                onParallelismChange = { input ->
                    handleParallelismInputChange(
                        input,
                        viewModel::updateParallelismInput,
                        viewModel::updateParallelism,
                        viewModel::updateParallelismError
                    )
                }
            )

            UrlListSection(
                modifier = Modifier.weight(4f),
                urlList = urlList,
                isDuration = isDuration,
                timeInput = timeInput,
                countInput = countInput,
                updateIndividualCount = viewModel::updateRequestCountByKey,
                updateIndividualTime = viewModel::updateTimeInMillisByKey,
                updateIndividualUnformattedTime = viewModel::updateUnformattedDurationValueByKey,
                updateIndividualIsEdit = viewModel::updateIsEditByKey,
                onRemoveUrl = { viewModel.updateUrlList(urlList.minus(it)) },
                onIndividualErrorChange = { individualErrorMessage = it }
            )
        }

        ErrorDialog(
            showDialog = showDialog,
            message = dialogErrorMessage,
            onDismiss = { viewModel.updateShowDialog(false) }
        )

        SettingsDialog(
            showDialog = showSettingsDialog,
            settings = testCaseSettings,
            onDismiss = { showSettingsDialog = false },
            onSave = { newSettings ->
                viewModel.updateTestCaseSettings(newSettings)
                viewModel.updateTestCase(testCase.copy(settings = newSettings))
                showSettingsDialog = false
            }
        )
    }
}
