package com.github.mikeandv.pingwatch.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.entity.TestCaseResult
import com.github.mikeandv.pingwatch.ui.viewmodels.MainScreenViewModel

@Composable
fun ReportScreen(viewModel: MainScreenViewModel, onNavigateBack: () -> Unit) {

    val testCase by viewModel.testCase.collectAsState()
    val scrollState = rememberScrollState()

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)

                ) {
                    Button(onClick = onNavigateBack) {
                        Text("Back to Home")
                    }
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color.LightGray,
                        thickness = 1.dp
                    )
                    buildTopUrls(testCase.testCaseResult)
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color.LightGray,
                        thickness = 1.dp
                    )
                    simpleTable(testCase.testCaseResult)
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    adapter = rememberScrollbarAdapter(scrollState)
                )
            }
        }
    }
}

@Composable
fun buildTopUrls(resultData: List<TestCaseResult>) {
    val topUrls = resultData.sortedByDescending { it.median }.take(5)
    val maxMedian = topUrls.maxOf { it.median.toFloat() }

    Text(
        text = "Top 5 URLs by Median Response Time",
        style = MaterialTheme.typography.subtitle1,
    )
    topUrls.forEach { result ->
        val normalizedMedian = (result.median.toFloat() / maxMedian) * 100

        // Line with URL and progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // URL
            BasicText(text = result.url, modifier = Modifier.weight(1f))

            // Progress bar
            Box(
                modifier = Modifier
                    .weight(3f)
                    .padding(end = 8.dp)
            ) {
                LinearProgressIndicator(
                    progress = normalizedMedian / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.primary
                )
            }

            // Median
            BasicText(text = "Median: ${result.median} ms", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun tableCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, Color.Gray)
            .padding(8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.body1)
    }
}

@Composable
fun simpleTable(resultData: List<TestCaseResult>) {
    // Headers
    Row(modifier = Modifier.fillMaxWidth().background(Color.LightGray)) {
        tableCell("URL", modifier = Modifier.weight(3f))
        tableCell("Total request", modifier = Modifier.weight(1f))
        tableCell("Error count", modifier = Modifier.weight(1f))
        tableCell("Min", modifier = Modifier.weight(1f))
        tableCell("Max", modifier = Modifier.weight(1f))
        tableCell("Avg", modifier = Modifier.weight(1f))
        tableCell("Median", modifier = Modifier.weight(1f))
        tableCell("P95", modifier = Modifier.weight(1f))
        tableCell("P99", modifier = Modifier.weight(1f))
    }

    // Data
    resultData.forEachIndexed { index, row ->
        val rowColor = if (index % 2 == 0) Color(0xFFE0E0E0) else Color.White

        Row(modifier = Modifier.fillMaxWidth().background(rowColor)) {
            tableCell(row.url, modifier = Modifier.weight(3f))
            tableCell("${row.totalRequestCount}", modifier = Modifier.weight(1f))
            tableCell("${row.errorRequestCount}", modifier = Modifier.weight(1f))
            tableCell("${row.min}", modifier = Modifier.weight(1f))
            tableCell("${row.max}", modifier = Modifier.weight(1f))
            tableCell("%.2f".format(row.avg), modifier = Modifier.weight(1f))
            tableCell("${row.median}", modifier = Modifier.weight(1f))
            tableCell("${row.p95}", modifier = Modifier.weight(1f))
            tableCell("${row.p99}", modifier = Modifier.weight(1f))
        }
    }
}
