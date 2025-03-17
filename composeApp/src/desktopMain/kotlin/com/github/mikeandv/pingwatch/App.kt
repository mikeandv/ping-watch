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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var text by remember { mutableStateOf("") }
        var itemList by remember { mutableStateOf(setOf<String>()) }
        var result by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope() // Создаем корутиновый scope для асинхронных операций

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
                    // Используем withContext(Dispatchers.IO), чтобы выполнить операцию в фоновом потоке
                    result = withContext(Dispatchers.IO) {
                        runDurationTest(itemList.toList(), 1) // Выполняем долгую операцию в фоновом потоке
                    }
                }
            }) {
                Text("Launch")
            }
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