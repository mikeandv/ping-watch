package com.github.mikeandv.pingwatch.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.domain.Category
import com.github.mikeandv.pingwatch.domain.TestCaseParams
import com.github.mikeandv.pingwatch.result.MetricStatistics
import com.github.mikeandv.pingwatch.result.TestCaseResult
import com.github.mikeandv.pingwatch.ui.components.CompareTagsDialog
import com.github.mikeandv.pingwatch.ui.components.TagDropdown
import com.github.mikeandv.pingwatch.ui.handlers.*
import com.github.mikeandv.pingwatch.ui.viewmodels.MainScreenViewModel
import com.github.mikeandv.pingwatch.utils.getCategory

@Composable
fun ReportScreen(viewModel: MainScreenViewModel, onNavigateBack: () -> Unit) {
    val testCase by viewModel.testCase.collectAsState()
    val urlList by viewModel.urlList.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val scrollState = rememberScrollState()
    var showCompareDialog by remember { mutableStateOf(false) }
    var comparisonResults by remember { mutableStateOf<List<TestCaseResult>>(emptyList()) }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState),
            ) {
                ReportToolbar(
                    tags = tags,
                    showClearComparison = comparisonResults.isNotEmpty(),
                    onNavigateBack = onNavigateBack,
                    onCompare = {
                        showCompareDialog = true
                        handleCompare(testCase, viewModel::updateTestCase, urlList)
                    },
                    onClearComparison = { comparisonResults = emptyList() }
                )

                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.LightGray,
                    thickness = 1.dp
                )

                TopUrlsSection(testCase.testCaseResult)
                Spacer(modifier = Modifier.height(16.dp))

                if (comparisonResults.isNotEmpty()) {
                    ResultsSection(
                        title = "Tag Comparison",
                        resultData = comparisonResults,
                        urlList = urlList,
                        tags = tags,
                        showTagColumn = false,
                        updateIndividualTag = viewModel::updateTagByKey,
                        onCreateTag = viewModel::addTag
                    )
                } else {
                    ResultsSection(
                        title = "Run Result",
                        resultData = testCase.testCaseResult,
                        urlList = urlList,
                        tags = tags,
                        showTagColumn = true,
                        updateIndividualTag = viewModel::updateTagByKey,
                        onCreateTag = viewModel::addTag
                    )
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd),
                adapter = rememberScrollbarAdapter(scrollState)
            )
        }

        CompareTagsDialog(
            showDialog = showCompareDialog,
            tags = tags,
            onDismiss = { showCompareDialog = false },
            onCompare = { firstTag, secondTag ->
                val allTimings = testCase.settings.agg.getAllTimings()
                val firstResult = TestCaseResult.createForTag(firstTag, testCase.urls, allTimings)
                val secondResult = TestCaseResult.createForTag(secondTag, testCase.urls, allTimings)
                comparisonResults = listOf(firstResult, secondResult)
                showCompareDialog = false
            }
        )
    }
}

@Composable
private fun ReportToolbar(
    tags: List<Category>,
    showClearComparison: Boolean,
    onNavigateBack: () -> Unit,
    onCompare: () -> Unit,
    onClearComparison: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onNavigateBack) {
            Text("Back to Home")
        }
        Button(
            onClick = onCompare,
            enabled = tags.size >= 2
        ) {
            Text("Compare")
        }
        if (showClearComparison) {
            Button(onClick = onClearComparison) {
                Text("Clear Comparison")
            }
        }
    }
}

@Composable
private fun ResultsSection(
    title: String,
    resultData: List<TestCaseResult>,
    urlList: Map<String, TestCaseParams>,
    tags: List<Category>,
    showTagColumn: Boolean,
    updateIndividualTag: (Category?, String) -> Unit,
    onCreateTag: (Category) -> Unit
) {
    Text(title, style = MaterialTheme.typography.subtitle1)
    Spacer(modifier = Modifier.height(8.dp))
    ResultsTable(
        resultData = resultData,
        urlList = urlList,
        tags = tags,
        showTagColumn = showTagColumn,
        updateIndividualTag = updateIndividualTag,
        onCreateTag = onCreateTag
    )
    Spacer(modifier = Modifier.height(16.dp))
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
private fun TableCell(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.body2
) {
    Box(
        modifier = modifier.padding(4.dp)
    ) {
        Text(text, style = style)
    }
}

@Composable
private fun ResultsTable(
    resultData: List<TestCaseResult>,
    urlList: Map<String, TestCaseParams>,
    tags: List<Category>,
    showTagColumn: Boolean,
    updateIndividualTag: (Category?, String) -> Unit,
    onCreateTag: (Category) -> Unit
) {
    if (resultData.isEmpty()) {
        Text(
            text = "No data to display",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
        return
    }

    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    TableHeader(showTagColumn)

    resultData.forEach { row ->
        val isExpanded = expanded[row.url] == true

        ResultRow(
            result = row,
            isExpanded = isExpanded,
            onExpandToggle = { expanded[row.url] = !isExpanded },
            selectedTag = getCategory(urlList, row.url),
            tags = tags,
            showTagColumn = showTagColumn,
            updateIndividualTag = updateIndividualTag,
            onCreateTag = onCreateTag
        )

        if (isExpanded) {
            ExpandedDetailsSection(result = row, showTagColumn = showTagColumn)
        }
    }
}

@Composable
private fun TableHeader(showTagColumn: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().background(Color.LightGray)) {
        TableCell("", modifier = Modifier.weight(TableColumnWeights.EXPAND_BUTTON))
        TableCell(if (showTagColumn) "URL" else "Tag", modifier = Modifier.weight(TableColumnWeights.URL))
        if (showTagColumn) {
            TableCell("Tag", modifier = Modifier.weight(TableColumnWeights.LABEL))
        }
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
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    selectedTag: Category?,
    tags: List<Category>,
    showTagColumn: Boolean,
    updateIndividualTag: (Category?, String) -> Unit,
    onCreateTag: (Category) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp).border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(TableColumnWeights.EXPAND_BUTTON),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = onExpandToggle) {
                Text(if (isExpanded) "−" else "+")
            }
        }

        TableCell(result.url, modifier = Modifier.weight(TableColumnWeights.URL))

        if (showTagColumn) {
            TagDropdown(
                selectedTag = selectedTag,
                tags = tags,
                onTagSelected = { tag -> updateIndividualTag(tag, result.url) },
                onCreateTag = onCreateTag,
                enabled = true,
                modifier = Modifier.weight(TableColumnWeights.LABEL)
            )
        }

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
private fun ExpandedDetailsSection(result: TestCaseResult, showTagColumn: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Network breakdown:", style = MaterialTheme.typography.subtitle2)

        getNetworkMetrics(result).forEach { metric ->
            NetworkMetricRow(metric.label, metric.stats, showTagColumn)
            Divider(color = Color.LightGray, thickness = 1.dp)
        }

        if (result.statusCodeCounts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Status breakdown:", style = MaterialTheme.typography.subtitle2)

            result.statusCodeCounts.entries.sortedBy { it.key }.forEach { (statusCode, count) ->
                breakDownRow("$statusCode", count, showTagColumn)
            }
        }

        if (result.errorsByType.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Network errors:", style = MaterialTheme.typography.subtitle2)

            result.errorsByType.forEach { (errorType, count) ->
                breakDownRow(errorTypeLabel(errorType), count, showTagColumn)
            }
        }
    }
}

@Composable
private fun NetworkMetricRow(label: String, stats: MetricStatistics?, showTagColumn: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TableCell("", modifier = Modifier.weight(TableColumnWeights.EXPAND_BUTTON))
        TableCell(label, modifier = Modifier.weight(TableColumnWeights.URL))
        if (showTagColumn) {
            TableCell("", modifier = Modifier.weight(TableColumnWeights.LABEL))
        }
        TableCell("", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell("", modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.min), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.max), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.avg), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.median), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.p95), modifier = Modifier.weight(TableColumnWeights.METRIC))
        TableCell(formatMetricValue(stats?.p99), modifier = Modifier.weight(TableColumnWeights.METRIC))
    }
}

@Composable
private fun breakDownRow(label: String, stat: Int, showTagColumn: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TableCell("", modifier = Modifier.weight(TableColumnWeights.EXPAND_BUTTON))
        TableCell(label, modifier = Modifier.weight(TableColumnWeights.URL))
        if (showTagColumn) {
            TableCell("", modifier = Modifier.weight(TableColumnWeights.LABEL))
        }
        TableCell("$stat", modifier = Modifier.weight(8f))
    }
}
