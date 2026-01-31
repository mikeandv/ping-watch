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
import com.github.mikeandv.pingwatch.domain.ExecutionMode

@Composable
fun ExecutionModeSelector(
    executionMode: ExecutionMode,
    onExecutionModeChange: (ExecutionMode) -> Unit,
    parallelismInput: String,
    parallelismError: String?,
    onParallelismChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Column {

        ExecutionModeSelectionRow(
            executionMode = executionMode,
            onExecutionModeChange = onExecutionModeChange,
            enabled = enabled
        )
        val isParallelEnabled = enabled && executionMode == ExecutionMode.PARALLEL
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Parallelism:",
                color = if (isParallelEnabled) Color.Unspecified else Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            CommonInputField(
                input = parallelismInput,
                onFieldChange = onParallelismChange,
                enabled = isParallelEnabled,
                fieldInputErrorMsg = parallelismError,
                modifier = Modifier.width(120.dp)
            )
        }
    }
}


@Composable
private fun ExecutionModeSelectionRow(
    executionMode: ExecutionMode,
    onExecutionModeChange: (ExecutionMode) -> Unit,
    enabled: Boolean = true
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
}

