package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.domain.RunType

@Composable
fun DurationOrCountSelector(
    runType: RunType,
    onDurationSelected: () -> Unit,
    onCountSelected: () -> Unit,
    durationErrorMessage: String?,
    timeInput: String,
    onTimeInputChange: (String) -> Unit,
    countInput: String,
    onCountInputChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Column {
        ModeSelectionRow(
            runType = runType,
            onDurationSelected = onDurationSelected,
            onCountSelected = onCountSelected,
            enabled = enabled
        )

        val (value, hint, onChange) = if (runType == RunType.DURATION) {
            Triple(timeInput, "Enter time (MM:SS)", onTimeInputChange)
        } else {
            Triple(countInput, "Enter the number", onCountInputChange)
        }

        CommonInputField(
            input = value,
            onFieldChange = onChange,
            enabled = enabled,
            fieldInputErrorMsg = durationErrorMessage,
            hintTest = hint,
            modifier = Modifier.width(200.dp)
        )
    }
}

@Composable
private fun ModeSelectionRow(
    runType: RunType,
    onDurationSelected: () -> Unit,
    onCountSelected: () -> Unit,
    enabled: Boolean = true
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Duration",
            color = if (enabled) {
                if (runType == RunType.DURATION) MaterialTheme.colors.primary else Color.Unspecified
            } else Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = runType == RunType.COUNT,
            onCheckedChange = { isCount ->
                if (isCount) onCountSelected() else onDurationSelected()
            },
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Count",
            color = if (enabled) {
                if (runType == RunType.COUNT) MaterialTheme.colors.primary else Color.Unspecified
            } else Color.Gray
        )
    }
}
