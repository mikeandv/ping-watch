package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.domain.RunType

@Composable
fun DurationOrCountSelector(
    runType: RunType,
    onDurationSelected: () -> Unit,
    onCountSelected: () -> Unit,
    timeInput: String,
    onTimeInputChange: (String) -> Unit,
    countInput: String,
    onCountInputChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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

        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            enabled = enabled,
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            modifier = Modifier
                .width(200.dp)
                .height(36.dp)
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
