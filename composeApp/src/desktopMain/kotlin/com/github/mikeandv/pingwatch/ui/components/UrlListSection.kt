package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.domain.RunType
import com.github.mikeandv.pingwatch.domain.TestCase
import com.github.mikeandv.pingwatch.utils.checkIsNotRunningStatus
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun UrlListSection(
    testCase: TestCase,
    modifier: Modifier = Modifier,
    timeInput: String,
    countInput: String,
    updateIndividualCount: (Long, String) -> Unit,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualIsEdit: (Boolean, String) -> Unit,
    onRemoveUrl: (String) -> Unit,
    onIndividualErrorChange: (String?) -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(end = 24.dp).verticalScroll(scrollState)
        ) {
            UrlListColumn(
                testCase = testCase,
                timeInput = timeInput,
                countInput = countInput,
                updateIndividualCount = updateIndividualCount,
                updateIndividualTime = updateIndividualTime,
                updateIndividualUnformattedTime = updateIndividualUnformattedTime,
                updateIndividualIsEdit = updateIndividualIsEdit,
                onRemoveUrl = onRemoveUrl,
                onIndividualErrorChange = onIndividualErrorChange
            )
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun UrlListColumn(
    testCase: TestCase,
    timeInput: String,
    countInput: String,
    updateIndividualCount: (Long, String) -> Unit,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualIsEdit: (Boolean, String) -> Unit,
    onRemoveUrl: (String) -> Unit,
    onIndividualErrorChange: (String?) -> Unit
) {
    val status by testCase.testCaseState.status.collectAsState()
    val isNotRunning = checkIsNotRunningStatus(status)

    testCase.urls.entries.forEachIndexed { index, entry ->
        UrlListItem(
            url = entry.key,
            params = entry.value,
            runType = testCase.runType,
            settings = testCase.settings,
            timeInput = timeInput,
            countInput = countInput,
            progressFlow = if (testCase.runType == RunType.COUNT) testCase.urlProgressFlow(entry.key) else emptyFlow(),
            updateIndividualCount = updateIndividualCount,
            updateIndividualTime = updateIndividualTime,
            updateIndividualUnformattedTime = updateIndividualUnformattedTime,
            updateIndividualIsEdit = updateIndividualIsEdit,
            onRemoveUrl = onRemoveUrl,
            onIndividualErrorChange = onIndividualErrorChange,
            enabled = isNotRunning
        )

        if (index != testCase.urls.size - 1) {
            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.LightGray,
                thickness = 1.dp
            )
        }
    }
}
