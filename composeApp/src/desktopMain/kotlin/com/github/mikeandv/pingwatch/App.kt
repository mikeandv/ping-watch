package com.github.mikeandv.pingwatch

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ReportScreen(reportData: List<TestCaseResult>, onNavigateBack: () -> Unit) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
//            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(onClick = onNavigateBack) {
                Text("Back to Home")
            }
            simpleTable(reportData)
        }
    }
}

@Composable
fun MainScreen(
//    reportData: List<TestCaseResult>,
    testCase0: TestCase,
    urlList: Set<String>,
    onUpdateState: (TestCase, Set<String>) -> Unit,
    onNavigate: () -> Unit
) {
    MaterialTheme {
        var url by remember { mutableStateOf("") }
        var testCount by remember { mutableStateOf("") }
        var isDuration by remember { mutableStateOf(true) }

        var timeInput by remember { mutableStateOf("") }
        var timeInMillis by remember { mutableStateOf<Long?>(null) }

        var urlErrorMessage by remember { mutableStateOf<String?>(null) }
        var durationErrorMessage by remember { mutableStateOf<String?>(null) }
        var dialogMessage by remember { mutableStateOf<String>("") }
        var showDialog by remember { mutableStateOf(false) }

        var progress by remember { mutableStateOf(0) } //

        val reportStatus by testCase0.testCaseState.status.collectAsState()


        val coroutineScope = rememberCoroutineScope()
        val urlPattern = Regex("^https?://.*")
        //Global column

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //Column for parameters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(4f)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .border(1.dp, Color.Red),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
//                            .fillMaxHeight()
                            .weight(1f),
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = url,
                            onValueChange = {
                                url = it
                                urlErrorMessage =
                                    if (it.matches(urlPattern) || it.isEmpty()) null else "URL must start with http:// or https://"
                            },
                            singleLine = true,
                            isError = urlErrorMessage != null,
                            label = { Text("Enter URL") }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 24.dp) // Minimum height so that the block is always
                        ) {
                            if (urlErrorMessage != null) {
                                Text(
                                    text = urlErrorMessage!!,
                                    color = MaterialTheme.colors.error,
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (url.matches(urlPattern)) {
                                onUpdateState(testCase0, urlList.plus(url))
//                                urlList = urlList.plus(url)
                                url = ""
                                urlErrorMessage = null
                            } else {
                                urlErrorMessage = "Incorrect URL"
                            }
                        },
//                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("Add URL")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            ""
                        },
//                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("Import..")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.fillMaxWidth().weight(1f).border(1.dp, Color.Cyan)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isDuration,
                                onClick = {
                                    isDuration = true
                                    durationErrorMessage = null
                                    testCount = ""
                                }
                            )
                            Text("Duration", modifier = Modifier.padding(start = 8.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(
                                selected = !isDuration,
                                onClick = {
                                    isDuration = false
                                    durationErrorMessage = null
                                    timeInMillis = null
                                }
                            )
                            Text("Count", modifier = Modifier.padding(start = 8.dp))
                        }
                        if (isDuration) {
                            // Duration input field
                            OutlinedTextField(
                                value = timeInput,
                                onValueChange = { input ->
                                    if (input.matches(Regex("^\\d{0,2}:?\\d{0,2}$"))) { // Format limitation
                                        timeInput = input
                                        durationErrorMessage = null

                                        // Parse MM:SS and convert to milliseconds
                                        val parts = input.split(":")
                                        if (parts.size == 2) {
                                            val minutes = parts[0].toIntOrNull() ?: 0
                                            val seconds = parts[1].toIntOrNull() ?: 0

                                            if (seconds in 0..59) {
                                                timeInMillis = (minutes * 60 + seconds) * 1000L
                                            } else {
                                                durationErrorMessage = "Seconds must be in the range 00-59"
                                            }
                                        }
                                    } else {
                                        durationErrorMessage = "Invalid format (MM:SS)"
                                    }
                                },
                                label = { Text("Enter time (MM:SS)") },
                                singleLine = true,
                                isError = durationErrorMessage != null,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Quantity input field
                            OutlinedTextField(
                                value = testCount,
                                onValueChange = { newText ->
                                    // Check if the entered value is a number
                                    durationErrorMessage = try {
                                        val number = newText.toInt()  // Trying to convert to Int
                                        null  // If it works, there are no errors.

                                    } catch (e: NumberFormatException) {
                                        "Enter the number"  // If there is an error, display an error message
                                    }
                                    testCount = newText
                                },
                                label = { Text("Enter the number of requests") },
                                singleLine = true,
                                isError = durationErrorMessage != null,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
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
//                    LaunchedEffect(progress) {
//                        if (testCase0.testCaseState.getStatus() == StatusCode.RUNNING || testCase0.testCaseState.getStatus() == StatusCode.CREATED) {
//                            while (testCase0.testCaseState.getStatus() == StatusCode.RUNNING || testCase0.testCaseState.getStatus() == StatusCode.CREATED) {
//                                delay(1000)
//                                progress = testCase0.getProgress()
//                            }
//                        }
//                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).border(1.dp, Color.Cyan),
                        horizontalAlignment = Alignment.CenterHorizontally, // For horizontal alignment
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            if (isDuration && (urlList.isEmpty() || timeInMillis == null || !durationErrorMessage.isNullOrEmpty())) {
                                dialogMessage = "Incorrect values for running the test!\n"
                                showDialog = true
                            } else if (!isDuration && (urlList.isEmpty() || testCount.isEmpty() || !durationErrorMessage.isNullOrEmpty())) {
                                dialogMessage = "Incorrect values for running the test!\n"
                                showDialog = true
                            } else {
                                val tmpTestCase0 = TestCase(
                                    urlList.toList(),
                                    if (isDuration) RunType.DURATION else RunType.COUNT,
                                    if (isDuration) 0 else testCount.toInt(),
                                    if (isDuration) timeInMillis else 0
                                )
                                onUpdateState(tmpTestCase0, urlList)

                                coroutineScope.launch {
                                    launch {
                                        tmpTestCase0.runTestCase()
                                    }
                                    launch {
                                        progress = 0
                                        while (tmpTestCase0.testCaseState.getStatus() == StatusCode.RUNNING || tmpTestCase0.testCaseState.getStatus() == StatusCode.CREATED) {
                                            delay(1000)
                                            progress = tmpTestCase0.getProgress()
                                        }
                                    }
                                }
                            }
                        }) {
                            Text("Launch")
                        }
//

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                onNavigate()
//                                onDataChange(currentResult)
                            },
                            enabled = reportStatus == StatusCode.FINISHED
                        ) {
                            Text("Get Result")
                        }
                    }
                }
            }

            // Colum for show progress
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .padding(10.dp),
//                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
//                Text(text = "Progress: ${progress}%")
                Text(text = "Progress: $progress%")

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = progress / 100f, // Преобразуем проценты в диапазон 0.0 - 1.0
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }

            // Colum to show list of urls added
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .padding(10.dp),
//                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                urlList.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(row, modifier = Modifier.weight(1f))
                        Button(onClick = { onUpdateState(testCase0, urlList.minus(row)) }
                        ) {
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
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                title = { Text("Error") },
                text = { Text(dialogMessage) },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Ок")
                    }
                }
            )
        }
    }
}

@Composable
@Preview
fun App() {
    var screen by remember { mutableStateOf("home") }
    var testCase0 by remember { mutableStateOf(TestCase(listOf(), RunType.COUNT, 0, 0)) }
    var urlList by remember { mutableStateOf(setOf<String>()) }

    when (screen) {
        "home" ->
            MainScreen(
                testCase0 = testCase0,
                urlList = urlList,
                onUpdateState = { newTestCase0, newUrlList ->
                    testCase0 = newTestCase0
                    urlList = newUrlList
                },
                onNavigate = { screen = "report" }
            )

        "report" -> ReportScreen(
            reportData = testCase0.testCaseResult,
            onNavigateBack = { screen = "home" }
        )
    }
}

