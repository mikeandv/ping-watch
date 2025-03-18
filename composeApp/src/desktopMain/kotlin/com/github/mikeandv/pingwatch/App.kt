package com.github.mikeandv.pingwatch

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var text by remember { mutableStateOf("") }
        var progress by remember { mutableStateOf(0) }
        var itemList by remember { mutableStateOf(setOf<String>()) }
        var result by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()
        var testCaseO by remember { mutableStateOf(TestCase(listOf(), RunType.COUNT, 0, 0)) }

        Column(modifier = Modifier.fillMaxWidth().padding(Dp(20.0F))) {
            OutlinedTextField(
                modifier = Modifier.width(Dp(300.0F)),
                value = text,
                onValueChange = { text = it },
                singleLine = true
            )
            Button(onClick = {
                itemList = itemList.plus(text)
                text = ""
            }) {
                Text("Add URL")
            }
            Button(onClick = {
                coroutineScope.launch {
                    launch {
                        testCaseO = TestCase(itemList.toList(), RunType.COUNT, 100, 0)
                        result = withContext(Dispatchers.IO) {
                            testCaseO.runTestCase()
                        }
                    }
                    launch {
                        progress = 0
                        delay(2000)
                        while (testCaseO.testCaseState.status == StatusCode.RUNNING || testCaseO.testCaseState.status == StatusCode.CREATED) {
                            delay(1000)
                            progress = testCaseO.getProgress()
                        }
                    }
                }
            }) {
                Text("Launch")
            }
//            LaunchedEffect(Unit) {}

            Text(text = "Progress: ${progress}%")
            Text(result)

            for (item in itemList) {
                Box(Modifier.fillMaxWidth()) {
                    Text(item)
                    Button(
                        onClick = { itemList = itemList.minus(item) },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}