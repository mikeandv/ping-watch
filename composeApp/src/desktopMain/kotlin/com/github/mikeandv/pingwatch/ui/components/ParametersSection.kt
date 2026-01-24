package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.entity.ExecutionMode

@Composable
fun ParametersSection(
    url: String,
    onUrlChange: (String) -> Unit,
    urlErrorMessage: String?,
    onAddUrl: () -> Unit,
    onImport: () -> Unit,
    isDuration: Boolean,
    onDurationSelected: () -> Unit,
    onCountSelected: () -> Unit,
    countInput: String,
    timeInput: String,
    onTimeInputChange: (String) -> Unit,
    onCountInputChange: (String) -> Unit,
    progress: Long,
    isDurationCountEnabled: Boolean,
    executionMode: ExecutionMode,
    onExecutionModeChange: (ExecutionMode) -> Unit,
    parallelismInput: String,
    parallelismError: String?,
    onParallelismChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DurationOrCountSelector(
                isDuration = isDuration,
                onDurationSelected = onDurationSelected,
                onCountSelected = onCountSelected,
                countInput = countInput,
                timeInput = timeInput,
                onTimeInputChange = onTimeInputChange,
                onCountInputChange = onCountInputChange,
                enabled = isDurationCountEnabled
            )

            Spacer(modifier = Modifier.width(32.dp))

            ExecutionModeSelector(
                executionMode = executionMode,
                onExecutionModeChange = onExecutionModeChange,
                parallelismInput = parallelismInput,
                parallelismError = parallelismError,
                onParallelismChange = onParallelismChange,
                enabled = isDurationCountEnabled
            )

            Spacer(modifier = Modifier.weight(1f))

            ProgressColumn(progress = progress)

            Spacer(modifier = Modifier.width(20.dp))
        }

        UrlInput(
            url = url,
            onUrlChange = onUrlChange,
            urlErrorMessage = urlErrorMessage,
            onAddUrl = onAddUrl,
            onImport = onImport
        )
    }
}
