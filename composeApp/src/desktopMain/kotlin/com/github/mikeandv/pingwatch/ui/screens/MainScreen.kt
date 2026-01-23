package com.github.mikeandv.pingwatch.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.StatusCode
import com.github.mikeandv.pingwatch.entity.ExecutionMode
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseParams
import com.github.mikeandv.pingwatch.entity.TestCaseSettings
import com.github.mikeandv.pingwatch.handlers.*
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
            val errorMessage = urlErrorMessage ?: durationErrorMessage ?: individualErrorMessage
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

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                FlowControlButtons(
                    testCase = testCase,
                    onLaunchTest = {
                        handleLaunchTest(
                            testCase, isDuration, { cancelFlag.get() }, urlList, durationErrorMessage, coroutineScope,
                            viewModel::updateTestCase, viewModel::updateProgress, viewModel::updateShowDialog,
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
                modifier = Modifier.weight(2f),
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
                        urlList,
                        { url = "" },
                        viewModel::updateUrlErrorMessage
                    )
                },
                onImport = {
                    handleImport(
                        viewModel::updateUrlList,
                        viewModel::updateShowDialog,
                        viewModel::updateDialogErrorMessage,
                        URL_PATTERN
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
                durationErrorMessage = durationErrorMessage,
                progress = progress
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

@Composable
private fun ParametersSection(
    modifier: Modifier = Modifier,
    url: String,
    onUrlChange: (String) -> Unit,
    urlErrorMessage: String?,
    onAddUrl: () -> Unit,
    onImport: () -> Unit,
    isDuration: Boolean,
    onDurationSelected: () -> Unit,
    onCountSelected: () -> Unit,
    countInput: String,
    timeInput: String,
    onTimeInputChange: (String) -> Unit,
    onCountInputChange: (String) -> Unit,
    durationErrorMessage: String?,
    progress: Long
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        UrlInput(
            url = url,
            onUrlChange = onUrlChange,
            urlErrorMessage = urlErrorMessage,
            onAddUrl = onAddUrl,
            onImport = onImport
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DurationOrCountSelector(
                isDuration = isDuration,
                onDurationSelected = onDurationSelected,
                onCountSelected = onCountSelected,
                countInput = countInput,
                timeInput = timeInput,
                onTimeInputChange = onTimeInputChange,
                durationErrorMessage = durationErrorMessage,
                onCountInputChange = onCountInputChange
            )

            Spacer(modifier = Modifier.weight(1f))

            ProgressColumn(progress = progress)

            Spacer(modifier = Modifier.width(20.dp))
        }
    }
}

@Composable
private fun UrlListSection(
    modifier: Modifier = Modifier,
    urlList: Map<String, TestCaseParams>,
    isDuration: Boolean,
    timeInput: String,
    countInput: String,
    updateIndividualCount: (Long, String) -> Unit,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualIsEdit: (Boolean, String) -> Unit,
    onRemoveUrl: (String) -> Unit,
    onIndividualErrorChange: (String?) -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(end = 12.dp).verticalScroll(scrollState)
        ) {
            UrlListColumn(
                urlList = urlList,
                isDuration = isDuration,
                timeInput = timeInput,
                countInput = countInput,
                updateIndividualCount = updateIndividualCount,
                updateIndividualTime = updateIndividualTime,
                updateIndividualUnformattedTime = updateIndividualUnformattedTime,
                updateIndividualIsEdit = updateIndividualIsEdit,
                onRemoveUrl = onRemoveUrl,
                onIndividualErrorChange = onIndividualErrorChange
            )
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun ErrorDialog(
    showDialog: Boolean,
    message: String?,
    onDismiss: () -> Unit
) {
    if (showDialog && message != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Ок")
                }
            }
        )
    }
}


@Composable
fun ProgressColumn(progress: Long) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        CircularProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 6.dp
        )
        Text(
            text = "$progress%",
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
fun UrlListColumn(
    urlList: Map<String, TestCaseParams>,
    isDuration: Boolean,
    timeInput: String,
    countInput: String,
    updateIndividualCount: (Long, String) -> Unit,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualIsEdit: (Boolean, String) -> Unit,
    onRemoveUrl: (String) -> Unit,
    onIndividualErrorChange: (String?) -> Unit
) {
    urlList.entries.forEachIndexed { index, entry ->
        UrlListItem(
            url = entry.key,
            params = entry.value,
            isDuration = isDuration,
            timeInput = timeInput,
            countInput = countInput,
            updateIndividualCount = updateIndividualCount,
            updateIndividualTime = updateIndividualTime,
            updateIndividualUnformattedTime = updateIndividualUnformattedTime,
            updateIndividualIsEdit = updateIndividualIsEdit,
            onRemoveUrl = onRemoveUrl,
            onIndividualErrorChange = onIndividualErrorChange
        )

        if (index != urlList.size - 1) {
            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.LightGray,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun UrlListItem(
    url: String,
    params: TestCaseParams,
    isDuration: Boolean,
    timeInput: String,
    countInput: String,
    updateIndividualCount: (Long, String) -> Unit,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualIsEdit: (Boolean, String) -> Unit,
    onRemoveUrl: (String) -> Unit,
    onIndividualErrorChange: (String?) -> Unit
) {
    var isChecked by remember { mutableStateOf(params.isEdit) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(url, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(16.dp))

        Text(text = "Edit", modifier = Modifier.weight(0.1f))
        Checkbox(
            modifier = Modifier.weight(0.1f),
            checked = isChecked,
            onCheckedChange = { checked ->
                isChecked = checked
                updateIndividualIsEdit(checked, url)
                syncIndividualValue(
                    isDuration, timeInput, countInput, url, updateIndividualTime,
                    updateIndividualUnformattedTime, updateIndividualCount,
                    onIndividualErrorChange
                )
            }
        )

        Box(modifier = Modifier.width(150.dp)) {
            if (isChecked) {
                IndividualInputField(
                    isDuration = isDuration,
                    params = params,
                    url = url,
                    updateIndividualTime = updateIndividualTime,
                    updateIndividualUnformattedTime = updateIndividualUnformattedTime,
                    updateIndividualCount = updateIndividualCount,
                    onErrorChange = onIndividualErrorChange
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))
        RemoveButton(onClick = { onRemoveUrl(url) })
    }
}

@Composable
private fun IndividualInputField(
    isDuration: Boolean,
    params: TestCaseParams,
    url: String,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualCount: (Long, String) -> Unit,
    onErrorChange: (String?) -> Unit
) {
    val (value, hint, onValueChange) = if (isDuration) {
        Triple(
            params.unformattedDurationValue,
            "Enter time (MM:SS)",
            { input: String ->
                handleIndividualTimeInputChange(
                    input,
                    url,
                    updateIndividualTime,
                    updateIndividualUnformattedTime,
                    onErrorChange
                )
            }
        )
    } else {
        Triple(
            if (params.countValue == 0L) "" else params.countValue.toString(),
            "Enter the number of requests",
            { input: String ->
                handleIndividualTestCountChange(input, url, updateIndividualCount, onErrorChange)
            }
        )
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        color = Color.Gray,
                        style = MaterialTheme.typography.body2
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun RemoveButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Remove",
            tint = MaterialTheme.colors.error
        )
    }
}

private fun syncIndividualValue(
    isDuration: Boolean,
    timeInput: String,
    countInput: String,
    url: String,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualCount: (Long, String) -> Unit,
    onErrorChange: (String?) -> Unit
) {
    if (isDuration) {
        handleIndividualTimeInputChange(
            timeInput,
            url,
            updateIndividualTime,
            updateIndividualUnformattedTime,
            onErrorChange
        )
    } else {
        handleIndividualTestCountChange(countInput, url, updateIndividualCount, onErrorChange)
    }
}

@Composable
fun UrlInput(
    url: String,
    onUrlChange: (String) -> Unit,
    urlErrorMessage: String?,
    onAddUrl: () -> Unit,
    onImport: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = url,
            onValueChange = onUrlChange,
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (url.isEmpty()) {
                        Text(
                            text = "Enter URL",
                            color = Color.Gray,
                            style = MaterialTheme.typography.body2
                        )
                    }
                    innerTextField()
                }
            }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Button(onClick = onAddUrl, enabled = url.isNotEmpty() && urlErrorMessage == null) {
            Text("Add URL")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(onClick = onImport) {
            Text("Import..")
        }
    }
}

@Composable
fun DurationOrCountSelector(
    isDuration: Boolean,
    onDurationSelected: () -> Unit,
    onCountSelected: () -> Unit,
    timeInput: String,
    onTimeInputChange: (String) -> Unit,
    durationErrorMessage: String?,
    countInput: String,
    onCountInputChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModeSelectionRow(
            isDuration = isDuration,
            onDurationSelected = onDurationSelected,
            onCountSelected = onCountSelected
        )

        val (value, hint, onChange) = if (isDuration) {
            Triple(timeInput, "Enter time (MM:SS)", onTimeInputChange)
        } else {
            Triple(countInput, "Enter the number of requests", onCountInputChange)
        }

        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            modifier = Modifier
                .width(200.dp)
                .height(36.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
                            color = Color.Gray,
                            style = MaterialTheme.typography.body2
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun ModeSelectionRow(
    isDuration: Boolean,
    onDurationSelected: () -> Unit,
    onCountSelected: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = isDuration, onClick = onDurationSelected)
        Text("Duration", modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(selected = !isDuration, onClick = onCountSelected)
        Text("Count", modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun FlowControlButtons(
    testCase: TestCase,
    onLaunchTest: () -> Unit,
    onNavigate: () -> Unit,
    cancelFlag: AtomicBoolean,
    updateShowDialog: (Boolean) -> Unit,
    updateDialogMessage: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val status by testCase.testCaseState.status.collectAsState()
    val isRunning = status == StatusCode.RUNNING
    val canLaunch = status == StatusCode.FINISHED || status == StatusCode.CREATED
    val canViewResult = status == StatusCode.FINISHED

    Row {
        LaunchButton(enabled = canLaunch, onClick = onLaunchTest)
        Spacer(modifier = Modifier.width(8.dp))
        CancelButton(enabled = isRunning) {
            cancelFlag.set(true)
            updateDialogMessage("Test canceled by user!")
            updateShowDialog(true)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(onClick = onNavigate, enabled = canViewResult) {
            Text("Get Result")
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSettingsClick, enabled = canLaunch) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = if (canLaunch) MaterialTheme.colors.primary else Color.Gray
            )
        }
    }
}

@Composable
private fun LaunchButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF41C300)),
        onClick = onClick,
        enabled = enabled
    ) {
        Text("Launch")
    }
}

@Composable
private fun CancelButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
        onClick = onClick,
        enabled = enabled
    ) {
        Text("Cancel")
    }
}

@Composable
private fun SettingsDialog(
    showDialog: Boolean,
    settings: TestCaseSettings,
    onDismiss: () -> Unit,
    onSave: (TestCaseSettings) -> Unit
) {
    if (showDialog) {
        var selectedMode by remember(settings) { mutableStateOf(settings.executionMode) }
        var parallelismInput by remember(settings) { mutableStateOf(settings.parallelism.toString()) }
        var parallelismError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Settings") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Execution Mode", style = MaterialTheme.typography.subtitle1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedMode == ExecutionMode.SEQUENTIAL,
                            onClick = { selectedMode = ExecutionMode.SEQUENTIAL }
                        )
                        Text("Sequential", modifier = Modifier.padding(start = 8.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = selectedMode == ExecutionMode.PARALLEL,
                            onClick = { selectedMode = ExecutionMode.PARALLEL }
                        )
                        Text("Parallel", modifier = Modifier.padding(start = 8.dp))
                    }

                    Text("Parallelism", style = MaterialTheme.typography.subtitle1)
                    BasicTextField(
                        value = parallelismInput,
                        onValueChange = { input ->
                            parallelismInput = input
                            val value = input.toIntOrNull()
                            parallelismError = when {
                                input.isEmpty() -> "Parallelism is required"
                                value == null -> "Must be a number"
                                value < 1 -> "Must be at least 1"
                                value > 64 -> "Must be at most 64"
                                else -> null
                            }
                        },
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colors.primary),
                        modifier = Modifier
                            .width(100.dp)
                            .height(36.dp)
                            .border(
                                1.dp,
                                if (parallelismError != null) MaterialTheme.colors.error else Color.Gray,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    parallelismError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parallelism = parallelismInput.toIntOrNull() ?: settings.parallelism
                        onSave(settings.copy(executionMode = selectedMode, parallelism = parallelism))
                    },
                    enabled = parallelismError == null
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
