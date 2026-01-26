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
import com.github.mikeandv.pingwatch.ui.handlers.handleMaxFileSizeInputChange
import com.github.mikeandv.pingwatch.ui.handlers.handleMaxLinesLimitInputChange

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

        val availableExtensions = listOf("txt", "json", "csv")
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
                            handleMaxFileSizeInputChange(input, { maxFileSizeInput = it }, { maxFileSizeError = it })
                        }
                    )

                    SettingsIntField(
                        label = "Max Lines Limit",
                        value = maxLinesLimitInput,
                        error = maxLinesLimitError,
                        onValueChange = { input ->
                            handleMaxLinesLimitInputChange(
                                input,
                                { maxLinesLimitInput = it },
                                { maxLinesLimitError = it })
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
                        onSave(
                            settings.copy(
                                maxFileSize = maxFileSize,
                                maxLinesLimit = maxLinesLimit,
                                allowedFileExtensions = selectedExtensions.toList()
                            )
                        )
                    },
                    enabled = maxFileSizeError == null && maxLinesLimitError == null &&
                            selectedExtensions.isNotEmpty()
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
                        },
                        //TODO remove when could handle other file extensions
                        enabled = ext == "txt"
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
