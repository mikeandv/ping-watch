package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ErrorDialog(
    showDialog: Boolean,
    message: String?,
    onDismiss: () -> Unit
) {
    if (showDialog && message != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Ок")
                }
            }
        )
    }
}
