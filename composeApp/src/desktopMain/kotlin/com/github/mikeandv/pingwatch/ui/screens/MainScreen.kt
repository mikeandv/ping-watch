package com.github.mikeandv.pingwatch.ui.screens


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.StatusCode
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.entity.TestCaseParams
import com.github.mikeandv.pingwatch.handlers.*
import com.github.mikeandv.pingwatch.ui.viewmodels.MainScreenViewModel
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    onNavigate: () -> Unit
) {
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
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val cancelFlag = AtomicBoolean(false)
    val urlPattern = Regex("^https?://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/\\S*)?$")

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Column for parameters
            Column(
                modifier = Modifier
                    .weight(4f)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // URL input, add, and import buttons
                UrlInput(
                    url = url,
                    onUrlChange = { input ->
                        handleUrlChange(
                            input = input,
                            updateUrl = { url = it },
                            updateErrorMessage = viewModel::updateUrlErrorMessage,
                            urlPattern = urlPattern
                        )
                    },
                    urlErrorMessage = urlErrorMessage,
                    onAddUrl = {
                        handleAddUrl(
                            url = url,
                            urlPattern = urlPattern,
                            updateUrlList = viewModel::updateUrlList,
                            currentUrls = urlList,
                            resetUrl = { url = "" },
                            updateErrorMessage = viewModel::updateUrlErrorMessage
                        )
                    },
                    onImport = {
                        handleImport(
                            updateUrlList = viewModel::updateUrlList,
                            updateDialogErrorMessage = viewModel::updateDialogErrorMessage,
                            updateShowDialog = viewModel::updateShowDialog,
                            urlPattern = urlPattern
                        )
                    }
                )
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {

                        // Duration/Count selection
                        DurationOrCountSelector(
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
                                    input = input,
                                    updateTimeInput = viewModel::updateTimeInput,
                                    updateErrorMessage = viewModel::updateDurationErrorMessage,
                                    updateTimeInMillis = viewModel::updateTimeInMillis
                                )
                            },
                            durationErrorMessage = durationErrorMessage,
                            onCountInputChange = { input ->
                                handleTestCountChange(
                                    input = input,
                                    updateCountInput = viewModel::updateCountInput,
                                    updateRequestCount = viewModel::updateRequestCount,
                                    updateErrorMessage = viewModel::updateDurationErrorMessage
                                )
                            },
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally, // For horizontal alignment
                        verticalArrangement = Arrangement.Center
                    ) {
                        FlowControlButtons(
                            testCase = testCase,
                            onLaunchTest = {
                                handleLaunchTest(
                                    testCase = testCase,
                                    isDuration = isDuration,
                                    urlList = urlList,
                                    durationErrorMessage = durationErrorMessage,
                                    coroutineScope = coroutineScope,
                                    onUpdateTestCase = viewModel::updateTestCase,
                                    updateProgress = viewModel::updateProgress,
                                    updateShowDialog = viewModel::updateShowDialog,
                                    updateDialogMessage = viewModel::updateDialogErrorMessage,
                                    cancelFlag = { cancelFlag.get() }
                                )
                            },
                            onNavigate = onNavigate,
                            cancelFlag = cancelFlag,
                            updateShowDialog = viewModel::updateShowDialog,
                            updateDialogMessage = viewModel::updateDialogErrorMessage
                        )
                    }
                }
            }

            // URL list column
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .verticalScroll(scrollState)
                ) {
                    UrlListColumn(
                        urlList = urlList,
                        isDuration = isDuration,
                        timeInput = timeInput,
                        countInput = countInput,
                        updateIndividualCount = viewModel::updateRequestCountByKey,
                        updateIndividualTime = viewModel::updateTimeInMillisByKey,
                        updateIndividualUnformattedTime = viewModel::updateUnformattedDurationValueByKey,
                        updateIndividualIsEdit = viewModel::updateIsEditByKey,
                        onRemoveUrl = { urlToRemove ->
                            viewModel.updateUrlList(urlList.minus(urlToRemove))
                        })
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    adapter = rememberScrollbarAdapter(scrollState)
                )

            }
            // Progress column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(10.dp),
            ) {
                ProgressColumn(progress = progress)
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.updateShowDialog(false)
                },
                title = { Text("Error") },
                text = { Text(dialogErrorMessage!!) },
                confirmButton = {
                    Button(onClick = { viewModel.updateShowDialog(false) }) {
                        Text("Ок")
                    }
                }
            )
        }
    }
}


@Composable
fun ProgressColumn(progress: Long) {
    Text(text = "Progress: $progress%")

    Spacer(modifier = Modifier.height(8.dp))

    LinearProgressIndicator(
        progress = progress / 100f, // Convert percentages to the range 0.0 - 1.0
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
    )
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
    onRemoveUrl: (String) -> Unit
) {
    urlList.entries.forEachIndexed { index, entry ->
        val key = entry.key
        val value = entry.value
        var isChecked by remember { mutableStateOf(value.isEdit) }
        var text by remember { mutableStateOf("") }

        var individualDurationErrorMessage by remember { mutableStateOf<String?>(null) }

        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(key, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Edit", modifier = Modifier.weight(0.1f))
            Checkbox(
                modifier = Modifier.weight(0.1f),
                checked = isChecked,
                onCheckedChange = { item ->
                    isChecked = item
                    updateIndividualIsEdit(item, key)
                    if (isDuration) {
                        handleIndividualTimeInputChange(
                            timeInput,
                            key,
                            updateTime = updateIndividualTime,
                            updateUnformattedTime = updateIndividualUnformattedTime,
                            updateErrorMessage = { individualDurationErrorMessage = it })
                    } else {
                        handleIndividualTestCountChange(
                            input = countInput,
                            key = key,
                            updateCount = updateIndividualCount,
                            updateErrorMessage = { individualDurationErrorMessage = it }
                        )
                    }
                })
            if (isChecked) {
                if (isDuration) {
                    // Duration input field
                    text = entry.value.unformattedDurationValue
                    OutlinedTextField(
                        value = text,
                        onValueChange = { input ->
                            handleIndividualTimeInputChange(
                                input = input,
                                key = key,
                                updateTime = updateIndividualTime,
                                updateUnformattedTime = updateIndividualUnformattedTime,
                                updateErrorMessage = { individualDurationErrorMessage = it }
                            )
                        },
                        label = {
                            if (individualDurationErrorMessage != null) {
                                Text(individualDurationErrorMessage.toString())
                            } else {
                                Text("Enter time (MM:SS)")
                            }
                        },
                        singleLine = true,
                        isError = individualDurationErrorMessage != null,
                        modifier = Modifier.width(250.dp)

                    )
                } else {
                    // Quantity input field
                    text = if (entry.value.countValue == 0L) "" else entry.value.countValue.toString()
                    OutlinedTextField(
                        value = text,
                        onValueChange = { input ->
                            handleIndividualTestCountChange(
                                input = input,
                                key = key,
                                updateCount = updateIndividualCount,
                                updateErrorMessage = { individualDurationErrorMessage = it }
                            )
                        },
                        label = {
                            if (individualDurationErrorMessage != null) {
                                Text(individualDurationErrorMessage.toString())
                            } else {
                                Text("Enter the number of requests")
                            }
                        },
                        singleLine = true,
                        isError = individualDurationErrorMessage != null,
                        modifier = Modifier.width(250.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(250.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = { onRemoveUrl(key) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colors.error
                )
            }
        }
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
fun UrlInput(
    url: String,
    onUrlChange: (String) -> Unit,
    urlErrorMessage: String?,
    onAddUrl: () -> Unit,
    onImport: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = url,
            onValueChange = { onUrlChange(it) },
            singleLine = true,
            isError = urlErrorMessage != null,
            label = {
                if (urlErrorMessage != null) {
                    Text(urlErrorMessage)
                } else {
                    Text("Enter URL")
                }
            }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onAddUrl,
            enabled = url.isNotEmpty() && urlErrorMessage == null
        ) {
            Text("Add URL")
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onImport
        ) {
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
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isDuration,
            onClick = onDurationSelected
        )
        Text("Duration", modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(
            selected = !isDuration,
            onClick = onCountSelected
        )
        Text("Count", modifier = Modifier.padding(start = 8.dp))
    }
    if (isDuration) {
        // Duration input field
        OutlinedTextField(
            value = timeInput,
            onValueChange = { onTimeInputChange(it) },
            label = {
                if (durationErrorMessage != null) {
                    Text(durationErrorMessage)
                } else {
                    Text("Enter time (MM:SS)")
                }
            },
            singleLine = true,
            isError = durationErrorMessage != null,
        )
    } else {
        // Quantity input field
        OutlinedTextField(
            value = countInput,
            onValueChange = { onCountInputChange(it) },
            label = {
                if (durationErrorMessage != null) {
                    Text(durationErrorMessage)
                } else {
                    Text("Enter the number of requests")
                }
            },
            singleLine = true,
            isError = durationErrorMessage != null,
        )
    }
}

@Composable
fun FlowControlButtons(
    testCase: TestCase,
    onLaunchTest: () -> Unit,
    onNavigate: () -> Unit,
    cancelFlag: AtomicBoolean,
    updateShowDialog: (Boolean) -> Unit,
    updateDialogMessage: (String) -> Unit
) {

    val status by testCase.testCaseState.status.collectAsState()

    Button(
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF41C300)  // Color green
        ),
        onClick = onLaunchTest,
        enabled = status == StatusCode.FINISHED || status == StatusCode.CREATED
    ) {
        Text("Launch")
    }
    Spacer(modifier = Modifier.width(16.dp))

    Button(
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.error  // Color Red
        ),
        onClick = {
            cancelFlag.set(true)
            updateDialogMessage("Test canceled by user!")
            updateShowDialog(true)
        },
        enabled = testCase.testCaseState.getStatus() == StatusCode.RUNNING
    ) {
        Text("Cancel")
    }
    Spacer(modifier = Modifier.width(16.dp))

    Button(
        onClick = {
            onNavigate()
        },
        enabled = testCase.testCaseState.getStatus() == StatusCode.FINISHED
    ) {
        Text("Get Result")
    }
}
