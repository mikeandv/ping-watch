package com.github.mikeandv.pingwatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.entity.TestCaseResult
import com.github.mikeandv.pingwatch.ui.viewmodels.MainScreenViewModel

@Composable
fun ReportScreen(viewModel: MainScreenViewModel, onNavigateBack: () -> Unit) {

    val testCase by viewModel.testCase.collectAsState()

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Button(onClick = onNavigateBack) {
                Text("Back to Home")
            }
            simpleTable(testCase.testCaseResult)
        }
    }
}


@Composable
fun tableCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, Color.Gray) // Обводка ячейки
            .padding(8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.body1)
    }
}

@Composable
fun simpleTable(resultData: List<TestCaseResult>) {

    Column(
        modifier = Modifier.border(2.dp, Color.Black, RoundedCornerShape(4.dp)) // Общая рамка таблицы
    ) {
        // Заголовки
        Row(modifier = Modifier.fillMaxWidth().background(Color.LightGray)) {
            tableCell("URL", modifier = Modifier.weight(2f))
            tableCell("Total request", modifier = Modifier.weight(2f))
            tableCell("Error count", modifier = Modifier.weight(2f))
            tableCell("Min", modifier = Modifier.weight(1f))
            tableCell("Max", modifier = Modifier.weight(1f))
            tableCell("Avg", modifier = Modifier.weight(1f))
            tableCell("Median", modifier = Modifier.weight(1f))
            tableCell("P95", modifier = Modifier.weight(1f))
            tableCell("P99", modifier = Modifier.weight(1f))
        }

        // Данные (чередование цветов строк)
        resultData.forEachIndexed { index, row ->
            val rowColor = if (index % 2 == 0) Color(0xFFE0E0E0) else Color.White // Серый / Белый фон

            Row(modifier = Modifier.fillMaxWidth().background(rowColor)) {
                tableCell(row.url, modifier = Modifier.weight(2f))
                tableCell("${row.totalRequestCount}", modifier = Modifier.weight(2f))
                tableCell("${row.errorRequestCount}", modifier = Modifier.weight(2f))
                tableCell("${row.min}", modifier = Modifier.weight(1f))
                tableCell("${row.max}", modifier = Modifier.weight(1f))
                tableCell("${"%.2f".format(row.avg)}", modifier = Modifier.weight(1f))
                tableCell("${row.median}", modifier = Modifier.weight(1f))
                tableCell("${row.p95}", modifier = Modifier.weight(1f))
                tableCell("${row.p99}", modifier = Modifier.weight(1f))
            }
        }
    }
}