package com.github.mikeandv.pingwatch.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.result.TestCaseResult
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
    Text(
        text = "Top 5 URLs by Median Response Time",
        style = MaterialTheme.typography.subtitle1,
    )

    if (resultData.isEmpty()) {
        Text(
            text = "No results available",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
        return
    }

    val topUrls = resultData.sortedByDescending { it.median }.take(5)
    val maxMedian = topUrls.maxOfOrNull { it.median.toFloat() } ?: 1f

    topUrls.forEach { result ->
        val normalizedMedian = if (maxMedian > 0) (result.median.toFloat() / maxMedian) * 100 else 0f

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicText(text = result.url, modifier = Modifier.weight(1f))

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

            BasicText(text = "Median: ${result.median} ms", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun tableCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
//            .border(1.dp, Color.Gray)
            .padding(8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.body1)
    }
}

@Composable
fun simpleTable(resultData: List<TestCaseResult>) {
    if (resultData.isEmpty()) {
        Text(
            text = "No data to display",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
        return
    }

    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    Row(modifier = Modifier.fillMaxWidth().background(Color.LightGray)) {
        tableCell("", modifier = Modifier.weight(0.4f)) // колонка под кнопку
        tableCell("URL", modifier = Modifier.weight(3f))
        tableCell("Total", modifier = Modifier.weight(1f))
        tableCell("Errors", modifier = Modifier.weight(1f))
        tableCell("Min", modifier = Modifier.weight(1f))
        tableCell("Max", modifier = Modifier.weight(1f))
        tableCell("Avg", modifier = Modifier.weight(1f))
        tableCell("Median", modifier = Modifier.weight(1f))
        tableCell("P95", modifier = Modifier.weight(1f))
        tableCell("P99", modifier = Modifier.weight(1f))
    }

    resultData.forEachIndexed { index, row ->
        val rowColor = if (index % 2 == 0) Color(0xFFE0E0E0) else Color.White
        val isExpanded = expanded[row.url] == true

        // Main row
        Row(modifier = Modifier.fillMaxWidth().background(rowColor)) {

            // Expand/collapse button
            Box(
                modifier = Modifier
                    .weight(0.4f),
//                    .border(1.dp, Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = { expanded[row.url] = !isExpanded }) {
                    Text(if (isExpanded) "−" else "+")
                }
            }

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

        // Expanded details row (your new metrics)
        if (isExpanded) {
            Row(modifier = Modifier.fillMaxWidth().background(rowColor)) {
                // пустая колонка под кнопку
                tableCell("", modifier = Modifier.weight(0.4f))

                // один широкий cell на всю остальную ширину
                Box(
                    modifier = Modifier
                        .weight(3f + 1f + 1f + 1f + 1f + 1f + 1f + 1f + 1f) // сумма весов остальных колонок
//                        .border(1.dp, Color.Gray)
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Network breakdown (avg):", style = MaterialTheme.typography.subtitle2)

                        // helper чтобы красиво печатать null
                        fun fmt(v: Long?) = v?.let { "${it} ms" } ?: "—"

                        Text("DNS: ${fmt(row.dnsMs)}")
                        Text("Connect: ${fmt(row.connectMs)}")
                        Text("TLS: ${fmt(row.tlsMs)}")
                        Text("Req headers: ${fmt(row.requestHeadersMs)}")
                        Text("Req body: ${fmt(row.requestBodyMs)}")
                        Text("Resp headers: ${fmt(row.responseHeadersMs)}")
                        Text("Resp body: ${fmt(row.responseBodyMs)}")
                    }
                }
            }
        }
    }
}

