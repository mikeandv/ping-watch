package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.domain.TestCaseParams
import com.github.mikeandv.pingwatch.ui.handlers.handleIndividualTestCountChange
import com.github.mikeandv.pingwatch.ui.handlers.handleIndividualTimeInputChange
import kotlinx.coroutines.flow.Flow

@Composable
fun UrlListItem(
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
        Text(url, modifier = Modifier.weight(1f), style = MaterialTheme.typography.body2)
        Spacer(modifier = Modifier.width(16.dp))

        if (!isDuration) {
            UrlProgressIndicator(progress)
            Spacer(modifier = Modifier.width(16.dp))
        }

        Text(text = "Edit", style = MaterialTheme.typography.body2)
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = { checked ->
                isChecked = checked
                updateIndividualIsEdit(checked, url)
                syncIndividualValue(
                    isDuration, timeInput, countInput, url,
                    updateIndividualTime, updateIndividualUnformattedTime,
                    updateIndividualCount, onIndividualErrorChange
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
        RemoveButton(onClick = { onRemoveUrl(url) }, enabled = enabled)
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
            timeInput, url, updateIndividualTime, updateIndividualUnformattedTime, onErrorChange
        )
    } else {
        handleIndividualTestCountChange(countInput, url, updateIndividualCount, onErrorChange)
    }
}
