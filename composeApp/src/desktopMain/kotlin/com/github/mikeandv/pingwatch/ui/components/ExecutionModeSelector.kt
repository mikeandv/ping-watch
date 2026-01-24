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
import com.github.mikeandv.pingwatch.entity.ExecutionMode

@Composable
fun ExecutionModeSelector(
    executionMode: ExecutionMode,
    onExecutionModeChange: (ExecutionMode) -> Unit,
    parallelismInput: String,
    parallelismError: String?,
    onParallelismChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Sequential",
                color = if (enabled) {
                    if (executionMode == ExecutionMode.SEQUENTIAL) MaterialTheme.colors.primary else Color.Unspecified
                } else Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = executionMode == ExecutionMode.PARALLEL,
                onCheckedChange = { isParallel ->
                    onExecutionModeChange(if (isParallel) ExecutionMode.PARALLEL else ExecutionMode.SEQUENTIAL)
                },
                enabled = enabled
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Parallel",
                color = if (enabled) {
                    if (executionMode == ExecutionMode.PARALLEL) MaterialTheme.colors.primary else Color.Unspecified
                } else Color.Gray
            )
        }

        val isParallelEnabled = enabled && executionMode == ExecutionMode.PARALLEL
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Parallelism:",
                color = if (isParallelEnabled) Color.Unspecified else Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = parallelismInput,
                onValueChange = onParallelismChange,
                singleLine = true,
                enabled = isParallelEnabled,
                cursorBrush = SolidColor(MaterialTheme.colors.primary),
                modifier = Modifier
                    .width(60.dp)
                    .height(36.dp)
                    .border(
                        1.dp,
                        when {
                            !isParallelEnabled -> Color.LightGray
                            parallelismError != null -> MaterialTheme.colors.error
                            else -> Color.Gray
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
