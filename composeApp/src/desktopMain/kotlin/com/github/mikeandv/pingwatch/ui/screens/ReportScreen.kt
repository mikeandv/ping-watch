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
import com.github.mikeandv.pingwatch.result.MetricStatistics
import com.github.mikeandv.pingwatch.result.TestCaseResult
import com.github.mikeandv.pingwatch.ui.handlers.*
import com.github.mikeandv.pingwatch.ui.viewmodels.MainScreenViewModel

@Composable
fun ReportScreen(viewModel: MainScreenViewModel, onNavigateBack: () -> Unit) {
    val testCase by viewModel.testCase.collectAsState()
    val scrollState = rememberScrollState()

    MaterialTheme {

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState),
            ) {
                Button(onClick = onNavigateBack) {
                    Text("Back to Home")
                }
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.LightGray,
                    thickness = 1.dp
                )
                TopUrlsSection(testCase.testCaseResult)
                Spacer(modifier = Modifier.height(16.dp))
                ResultsTable(testCase.testCaseResult)
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd),
                adapter = rememberScrollbarAdapter(scrollState)
            )
        }

    }
}

@Composable
private fun TopUrlsSection(resultData: List<TestCaseResult>) {
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

    val topUrls = getTopUrlsByMedian(resultData)
    val maxMedian = topUrls.maxOfOrNull { it.median } ?: 0.0

    topUrls.forEach { result ->
        val normalizedMedian = calculateNormalizedMedian(result.median, maxMedian)

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
                    progress = (normalizedMedian / 100).toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.primary
                )
            }

            BasicText(text = "Median: ${result.median} ms", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TableCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.body2)
    }
}

@Composable
private fun ResultsTable(resultData: List<TestCaseResult>) {
    if (resultData.isEmpty()) {
        Text(
            text = "No data to display",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
        return
    }

    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    TableHeader()

    resultData.forEachIndexed { index, row ->
        val rowColor = if (index % 2 == 0) Color(0xFFE0E0E0) else Color.White
        val isExpanded = expanded[row.url] == true

        ResultRow(
            result = row,
            rowColor = rowColor,
            isExpanded = isExpanded,
            onExpandToggle = { expanded[row.url] = !isExpanded }
        )

        if (isExpanded) {
            ExpandedDetailsSection(result = row, rowColor = rowColor)
        }
    }
}

@Composable
private fun TableHeader() {
    Row(modifier = Modifier.fillMaxWidth().background(Color.LightGray)) {
        TableCell("", modifier = Modifier.weight(TableColumnWeights.EXPAND_BUTTON))
        TableCell("URL", modifier = Modifier.weight(TableColumnWeights.URL))
        TableCell("Total", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell("Errors", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell("Min", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell("Max", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell("Avg", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell("Median", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell("P95", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell("P99", modifier = Modifier.weight(TableColumnWeights.METRIC))
    }
}

@Composable
private fun ResultRow(
    result: TestCaseResult,
    rowColor: Color,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().background(rowColor).padding(8.dp)) {
        Box(
            modifier = Modifier.weight(TableColumnWeights.EXPAND_BUTTON),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = onExpandToggle) {
                Text(if (isExpanded) "−" else "+")
            }
        }

        TableCell(result.url, modifier = Modifier.weight(TableColumnWeights.URL))
        TableCell("${result.totalRequestCount}", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell("${result.errorRequestCount}", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(result.min), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(result.max), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(result.avg), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(result.median), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(result.p95), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(result.p99), modifier = Modifier.weight(TableColumnWeights.METRIC))
    }
}

@Composable
private fun ExpandedDetailsSection(result: TestCaseResult, rowColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().background(rowColor).padding(8.dp)) {
        Text("Network breakdown:", style = MaterialTheme.typography.subtitle2)

        getNetworkMetrics(result).forEach { metric ->
            NetworkMetricRow(metric.label, metric.stats)
        }

        if (result.errorsByType.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Error breakdown:", style = MaterialTheme.typography.subtitle2)

            result.errorsByType.forEach { (errorType, count) ->
                Text("${errorTypeLabel(errorType)}: $count", style = MaterialTheme.typography.body2)
            }
        }
    }
}

@Composable
private fun NetworkMetricRow(label: String, stats: MetricStatistics?) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TableCell("", modifier = Modifier.weight(TableColumnWeights.EXPAND_BUTTON))
        TableCell(" ", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(label, modifier = Modifier.weight(TableColumnWeights.LABEL))
        TableCell("-", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell("-", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.min), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.max), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.avg), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.median), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.p95), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.p99), modifier = Modifier.weight(TableColumnWeights.METRIC))
    }
}
