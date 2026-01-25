package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.utils.checkIsNotRunningStatus
import com.github.mikeandv.pingwatch.domain.TestCase
import com.github.mikeandv.pingwatch.domain.TestCaseParams
import com.github.mikeandv.pingwatch.ui.handlers.handleIndividualTestCountChange
import com.github.mikeandv.pingwatch.ui.handlers.handleIndividualTimeInputChange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun UrlListSection(
    modifier: Modifier = Modifier,
    urlList: Map<String, TestCaseParams>,
    isDuration: Boolean,
    timeInput: String,
    countInput: String,
    testCase: TestCase,
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
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(end = 12.dp).verticalScroll(scrollState)
        ) {
            UrlListColumn(
                urlList = urlList,
                isDuration = isDuration,
                timeInput = timeInput,
                countInput = countInput,
                testCase = testCase,
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
fun UrlListColumn(
    urlList: Map<String, TestCaseParams>,
    isDuration: Boolean,
    timeInput: String,
    countInput: String,
    testCase: TestCase,
    updateIndividualCount: (Long, String) -> Unit,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualIsEdit: (Boolean, String) -> Unit,
    onRemoveUrl: (String) -> Unit,
    onIndividualErrorChange: (String?) -> Unit
) {
    val status by testCase.testCaseState.status.collectAsState()
    val isNotRunning = checkIsNotRunningStatus(status)

    urlList.entries.forEachIndexed { index, entry ->
        UrlListItem(
            url = entry.key,
            params = entry.value,
            isDuration = isDuration,
            timeInput = timeInput,
            countInput = countInput,
            progressFlow = if (!isDuration) testCase.urlProgressFlow(entry.key) else emptyFlow(),
            updateIndividualCount = updateIndividualCount,
            updateIndividualTime = updateIndividualTime,
            updateIndividualUnformattedTime = updateIndividualUnformattedTime,
            updateIndividualIsEdit = updateIndividualIsEdit,
            onRemoveUrl = onRemoveUrl,
            onIndividualErrorChange = onIndividualErrorChange,
            enabled = isNotRunning
        )

        if (index != urlList.size - 1) {
            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.LightGray,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun UrlListItem(
    url: String,
    params: TestCaseParams,
    isDuration: Boolean,
    timeInput: String,
    countInput: String,
    progressFlow: Flow<Int>,
    updateIndividualCount: (Long, String) -> Unit,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualIsEdit: (Boolean, String) -> Unit,
    onRemoveUrl: (String) -> Unit,
    onIndividualErrorChange: (String?) -> Unit,
    enabled: Boolean
) {
    var isChecked by remember { mutableStateOf(params.isEdit) }
    val progress by progressFlow.collectAsState(initial = 0)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(url, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(16.dp))

        if (!isDuration) {
            UrlProgressIndicator(progress)
            Spacer(modifier = Modifier.width(16.dp))
        }

        Text(text = "Edit")
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = { checked ->
                isChecked = checked
                updateIndividualIsEdit(checked, url)
                syncIndividualValue(
                    isDuration, timeInput, countInput, url, updateIndividualTime,
                    updateIndividualUnformattedTime, updateIndividualCount,
                    onIndividualErrorChange
                )
            },
            enabled = enabled
        )

        Box(modifier = Modifier.width(150.dp)) {
            if (isChecked) {
                IndividualInputField(
                    isDuration = isDuration,
                    params = params,
                    url = url,
                    updateIndividualTime = updateIndividualTime,
                    updateIndividualUnformattedTime = updateIndividualUnformattedTime,
                    updateIndividualCount = updateIndividualCount,
                    onErrorChange = onIndividualErrorChange,
                    enabled = enabled
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))
        RemoveButton(
            onClick = { onRemoveUrl(url) },
            enabled = enabled
        )
    }
}

@Composable
private fun UrlProgressIndicator(progress: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(18.dp)
    ) {
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 3.dp,
            color = MaterialTheme.colors.primary.copy(alpha = 0.2f)
        )
        CircularProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 3.dp
        )
//        Text(
//            text = "$progress%",
//            style = MaterialTheme.typography.caption,
//            maxLines = 1
//        )
    }
}

@Composable
private fun IndividualInputField(
    isDuration: Boolean,
    params: TestCaseParams,
    url: String,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualCount: (Long, String) -> Unit,
    onErrorChange: (String?) -> Unit,
    enabled: Boolean
) {
    val (value, hint, onValueChange) = if (isDuration) {
        Triple(
            params.unformattedDurationValue,
            "Enter time (MM:SS)",
            { input: String ->
                handleIndividualTimeInputChange(
                    input,
                    url,
                    updateIndividualTime,
                    updateIndividualUnformattedTime,
                    onErrorChange
                )
            }
        )
    } else {
        Triple(
            if (params.countValue == 0L) "" else params.countValue.toString(),
            "Enter the number",
            { input: String ->
                handleIndividualTestCountChange(input, url, updateIndividualCount, onErrorChange)
            }
        )
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        enabled = enabled,
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, if (enabled) Color.Gray else Color.LightGray, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        color = Color.Gray,
                        style = MaterialTheme.typography.body2
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun RemoveButton(onClick: () -> Unit, enabled: Boolean) {
    IconButton(onClick = onClick, modifier = Modifier.size(18.dp), enabled = enabled) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Remove",
            tint = if (enabled) Color.Gray else Color.LightGray
        )
    }
}

private fun syncIndividualValue(
    isDuration: Boolean,
    timeInput: String,
    countInput: String,
    url: String,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualCount: (Long, String) -> Unit,
    onErrorChange: (String?) -> Unit
) {
    if (isDuration) {
        handleIndividualTimeInputChange(
            timeInput,
            url,
            updateIndividualTime,
            updateIndividualUnformattedTime,
            onErrorChange
        )
    } else {
        handleIndividualTestCountChange(countInput, url, updateIndividualCount, onErrorChange)
    }
}
