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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.domain.TestCase
import com.github.mikeandv.pingwatch.utils.checkIsNotRunningStatus

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
    onIndividualErrorChange: (String, String?) -> Unit,
    individualErrorMsgMap: Map<String, String?>
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState).padding(horizontal = 12.dp)
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
                onIndividualErrorChange = onIndividualErrorChange,
                individualErrorMsgMap = individualErrorMsgMap
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
    onIndividualErrorChange: (String, String?) -> Unit,
    individualErrorMsgMap: Map<String, String?>
) {
    val status by testCase.testCaseState.status.collectAsState()
    val runId by testCase.testCaseState.runId.collectAsState()
    val isNotRunning = checkIsNotRunningStatus(status)

    testCase.urls.entries.forEachIndexed { index, entry ->
        val progressFlow = remember(runId, entry.key) {
            testCase.urlProgressFlow(entry.key)
        }

        UrlListItem(
            url = entry.key,
            params = entry.value,
            runType = testCase.runType,
            settings = testCase.settings,
            timeInput = timeInput,
            countInput = countInput,
            progressFlow = progressFlow,
            updateIndividualCount = updateIndividualCount,
            updateIndividualTime = updateIndividualTime,
            updateIndividualUnformattedTime = updateIndividualUnformattedTime,
            updateIndividualIsEdit = updateIndividualIsEdit,
            onRemoveUrl = onRemoveUrl,
            onIndividualErrorChange = onIndividualErrorChange,
            individualErrorMsg = individualErrorMsgMap[entry.key],
            enabled = isNotRunning
        )

        if (index != testCase.urls.size - 1) {
            Divider(
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 2.dp),
                color = Color.LightGray,
                thickness = 1.dp
            )
        }
    }
}
