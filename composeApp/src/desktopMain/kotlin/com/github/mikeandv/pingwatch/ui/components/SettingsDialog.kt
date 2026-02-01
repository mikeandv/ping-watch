package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.domain.TestCaseSettings
import com.github.mikeandv.pingwatch.ui.handlers.handleIntInputChange

@Composable
fun SettingsDialog(
    showDialog: Boolean,
    settings: TestCaseSettings,
    onDismiss: () -> Unit,
    onSave: (TestCaseSettings) -> Unit
) {
    if (showDialog) {
        var maxFileSizeInput by remember(settings) { mutableStateOf(settings.maxFileSize.toString()) }
        var maxFileSizeError by remember { mutableStateOf<String?>(null) }
        var maxLinesLimitInput by remember(settings) { mutableStateOf(settings.maxLinesLimit.toString()) }
        var maxLinesLimitError by remember { mutableStateOf<String?>(null) }
        var earlyStopThresholdInput by remember(settings) { mutableStateOf(settings.earlyStopThreshold.toString()) }
        var earlyStopThresholdError by remember { mutableStateOf<String?>(null) }
        var dispatcherMaxRequestsInput by remember(settings) { mutableStateOf(settings.dispatcherMaxRequests.toString()) }
        var dispatcherMaxRequestsError by remember { mutableStateOf<String?>(null) }
        var dispatcherMaxRequestsPerHostInput by remember(settings) { mutableStateOf(settings.dispatcherMaxRequestsPerHost.toString()) }
        var dispatcherMaxRequestsPerHostError by remember { mutableStateOf<String?>(null) }

        val availableExtensions = listOf("txt")
        var selectedExtensions by remember(settings) { mutableStateOf(settings.allowedFileExtensions.toSet()) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Settings") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsIntField(
                        label = "Max File Size (MB)",
                        value = maxFileSizeInput,
                        error = maxFileSizeError,
                        onValueChange = { input ->
                            handleIntInputChange(
                                input,
                                { maxFileSizeInput = it },
                                { maxFileSizeError = it },
                                settings.minCommonInput,
                                settings.maxFileSizeInput
                            )
                        }
                    )

                    SettingsIntField(
                        label = "Max Lines Limit",
                        value = maxLinesLimitInput,
                        error = maxLinesLimitError,
                        onValueChange = { input ->
                            handleIntInputChange(
                                input,
                                { maxLinesLimitInput = it },
                                { maxLinesLimitError = it },
                                settings.minCommonInput,
                                settings.maxLineLimitInput
                            )
                        }
                    )

                    SettingsIntField(
                        label = "Early Stop Threshold",
                        value = earlyStopThresholdInput,
                        error = earlyStopThresholdError,
                        onValueChange = { input ->
                            handleIntInputChange(
                                input,
                                { earlyStopThresholdInput = it },
                                { earlyStopThresholdError = it },
                                settings.minCommonInput,
                                settings.maxEarlyStopThresholdInput
                            )
                        }
                    )

                    SettingsIntField(
                        label = "Dispatcher Max Requests",
                        value = dispatcherMaxRequestsInput,
                        error = dispatcherMaxRequestsError,
                        onValueChange = { input ->
                            handleIntInputChange(
                                input,
                                { dispatcherMaxRequestsInput = it },
                                { dispatcherMaxRequestsError = it },
                                settings.minCommonInput,
                                settings.maxDispatcherRequestsInput
                            )
                        }
                    )

                    SettingsIntField(
                        label = "Per Host Dispatcher Max Requests ",
                        value = dispatcherMaxRequestsPerHostInput,
                        error = dispatcherMaxRequestsPerHostError,
                        onValueChange = { input ->
                            handleIntInputChange(
                                input,
                                { dispatcherMaxRequestsPerHostInput = it },
                                { dispatcherMaxRequestsPerHostError = it },
                                settings.minCommonInput,
                                settings.maxDispatcherRequestsInput
                            )
                        }
                    )

                    FileExtensionsSelector(
                        availableExtensions = availableExtensions,
                        selectedExtensions = selectedExtensions,
                        onSelectionChange = { selectedExtensions = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val maxFileSize = maxFileSizeInput.toIntOrNull() ?: settings.maxFileSize
                        val maxLinesLimit = maxLinesLimitInput.toIntOrNull() ?: settings.maxLinesLimit
                        val earlyStopThreshold = earlyStopThresholdInput.toIntOrNull() ?: settings.earlyStopThreshold
                        val dispatcherMaxRequests =
                            dispatcherMaxRequestsInput.toIntOrNull() ?: settings.dispatcherMaxRequests
                        val dispatcherMaxRequestsPerHost =
                            dispatcherMaxRequestsPerHostInput.toIntOrNull() ?: settings.dispatcherMaxRequestsPerHost
                        onSave(
                            settings.copy(
                                maxFileSize = maxFileSize,
                                maxLinesLimit = maxLinesLimit,
                                allowedFileExtensions = selectedExtensions.toList(),
                                earlyStopThreshold = earlyStopThreshold,
                                dispatcherMaxRequests = dispatcherMaxRequests,
                                dispatcherMaxRequestsPerHost = dispatcherMaxRequestsPerHost
                            )
                        )
                    },
                    enabled = maxFileSizeError == null && maxLinesLimitError == null &&
                            earlyStopThresholdError == null &&
                            dispatcherMaxRequestsError == null && dispatcherMaxRequestsPerHostError == null && selectedExtensions.isNotEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsIntField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.subtitle1)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            modifier = Modifier
                .width(100.dp)
                .height(36.dp)
                .border(
                    1.dp,
                    if (error != null) MaterialTheme.colors.error else Color.Gray,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
        Text(
            text = error ?: " ",
            color = if (error != null) MaterialTheme.colors.error else Color.Transparent,
            style = MaterialTheme.typography.caption
        )
    }
}

@Composable
private fun FileExtensionsSelector(
    availableExtensions: List<String>,
    selectedExtensions: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    Column {
        Text("Allowed File Extensions", style = MaterialTheme.typography.subtitle1)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            availableExtensions.forEach { ext ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedExtensions.contains(ext),
                        onCheckedChange = { checked ->
                            onSelectionChange(
                                if (checked) selectedExtensions + ext else selectedExtensions - ext
                            )
                        }
                    )
                    Text(ext)
                }
            }
        }
        Text(
            text = if (selectedExtensions.isEmpty()) "At least one extension required" else " ",
            color = if (selectedExtensions.isEmpty()) MaterialTheme.colors.error else Color.Transparent,
            style = MaterialTheme.typography.caption
        )
    }
}
