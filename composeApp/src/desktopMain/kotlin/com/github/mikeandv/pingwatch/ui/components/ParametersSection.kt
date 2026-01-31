package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.domain.ExecutionMode
import com.github.mikeandv.pingwatch.domain.TestCase
import com.github.mikeandv.pingwatch.utils.checkIsNotRunningStatus

@Composable
fun ParametersSection(
    testCase: TestCase,
    url: String,
    onUrlChange: (String) -> Unit,
    urlErrorMessage: String?,
    onAddUrl: () -> Unit,
    onImport: () -> Unit,
    onDurationSelected: () -> Unit,
    onCountSelected: () -> Unit,
    countInput: String,
    timeInput: String,
    durationErrorMessage: String?,
    onTimeInputChange: (String) -> Unit,
    onCountInputChange: (String) -> Unit,
    progress: Long,
    onExecutionModeChange: (ExecutionMode) -> Unit,
    parallelismInput: String,
    parallelismError: String?,
    onParallelismChange: (String) -> Unit
) {
    val status by testCase.testCaseState.status.collectAsState()
    val isNotRunning = checkIsNotRunningStatus(status)
    val isEnabled = testCase.urls.isNotEmpty()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DurationOrCountSelector(
                runType = testCase.runType,
                onDurationSelected = onDurationSelected,
                onCountSelected = onCountSelected,
                countInput = countInput,
                timeInput = timeInput,
                durationErrorMessage = durationErrorMessage,
                onTimeInputChange = onTimeInputChange,
                onCountInputChange = onCountInputChange,
                enabled = isEnabled && isNotRunning
            )

            Spacer(modifier = Modifier.width(32.dp))

            ExecutionModeSelector(
                executionMode = testCase.executionMode,
                onExecutionModeChange = onExecutionModeChange,
                parallelismInput = parallelismInput,
                parallelismError = parallelismError,
                onParallelismChange = onParallelismChange,
                enabled = isEnabled && isNotRunning
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
            onImport = onImport,
            enabled = isNotRunning
        )
    }
}
