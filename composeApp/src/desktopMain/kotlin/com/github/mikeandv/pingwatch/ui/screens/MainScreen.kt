package com.github.mikeandv.pingwatch.ui.screens


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.StatusCode
import com.github.mikeandv.pingwatch.entity.TestCase
import com.github.mikeandv.pingwatch.handlers.*
import com.github.mikeandv.pingwatch.ui.viewmodels.MainScreenViewModel
import java.awt.FileDialog
import java.awt.Frame

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
    val timeInMillis by viewModel.timeInMillis.collectAsState()
    val requestCount by viewModel.requestCount.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()

    var url by remember { mutableStateOf("") }
    var countInput by remember { mutableStateOf("") }
    var isDuration by remember { mutableStateOf(true) }
    var timeInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()
    val urlPattern = Regex("^https?://.*")

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
                            updateShowDialog = viewModel::updateShowDialog
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
                                isDuration = true
                                viewModel.updateDurationErrorMessage(null)
                                countInput = ""
                            },
                            onCountSelected = {
                                isDuration = false
                                viewModel.updateDurationErrorMessage(null)
                                timeInput = ""
                            },
                            countInput = countInput,
                            timeInput = timeInput,
                            onTimeInputChange = { input ->
                                handleTimeInputChange(
                                    input = input,
                                    updateTimeInput = { timeInput = it },
                                    updateErrorMessage = viewModel::updateDurationErrorMessage,
                                    updateTimeInMillis = viewModel::updateTimeInMillis
                                )
                            },
                            durationErrorMessage = durationErrorMessage,
                            onCountInputChange = { input ->
                                handleTestCountChange(
                                    input = input,
                                    updateCountInput = { countInput = it },
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
                        LaunchAndResultButtons(
                            testCase = testCase,
                            onLaunchTest = {
                                handleLaunchTest(
                                    isDuration = isDuration,
                                    urlList = urlList,
                                    requestCount = requestCount,
                                    timeInMillis = timeInMillis,
                                    durationErrorMessage = durationErrorMessage,
                                    coroutineScope = coroutineScope,
                                    onUpdateTestCase = viewModel::updateTestCase,
                                    updateProgress = viewModel::updateProgress,
                                    updateShowDialog = viewModel::updateShowDialog,
                                    updateDialogMessage = viewModel::updateDialogErrorMessage
                                )
                            },
                            onNavigate = onNavigate
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
                    UrlListColumn(urlList = urlList,
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
fun UrlListColumn(urlList: Set<String>, onRemoveUrl: (String) -> Unit) {
    urlList.forEachIndexed { index, row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(row, modifier = Modifier.weight(1f))
            Button(onClick = { onRemoveUrl(row) }) {
                Text("Delete")
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
        Column(
            modifier = Modifier.weight(1f),
        ) {
            OutlinedTextField(
                value = url,
                onValueChange = { onUrlChange(it) },
                singleLine = true,
                isError = urlErrorMessage != null,
                label = { Text("Enter URL") }
            )
            Box(
                modifier = Modifier
                    .heightIn(min = 24.dp) // Minimum height so that the block is always
            ) {
                if (urlErrorMessage != null) {
                    Text(
                        text = urlErrorMessage,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        }

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
            label = { Text("Enter time (MM:SS)") },
            singleLine = true,
            isError = durationErrorMessage != null,
        )
    } else {
        // Quantity input field
        OutlinedTextField(
            value = countInput,
            onValueChange = { onCountInputChange(it) },
            label = { Text("Enter the number of requests") },
            singleLine = true,
            isError = durationErrorMessage != null,
        )
    }
    Box(
        modifier = Modifier
            .heightIn(min = 24.dp) // Minimum height so that the block is always
    ) {
        if (durationErrorMessage != null) {
            Text(
                text = durationErrorMessage!!,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun LaunchAndResultButtons(
    testCase: TestCase,
    onLaunchTest: () -> Unit,
    onNavigate: () -> Unit
) {

    val status by testCase.testCaseState.status.collectAsState()

    Button(
        onClick = onLaunchTest,
        enabled = status == StatusCode.FINISHED || status == StatusCode.CREATED
    ) {
        Text("Launch")
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



