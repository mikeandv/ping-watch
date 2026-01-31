package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UrlInput(
    url: String,
    onUrlChange: (String) -> Unit,
    urlErrorMessage: String?,
    onAddUrl: () -> Unit,
    onImport: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(70.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommonInputField(
            input = url,
            onFieldChange = onUrlChange,
            enabled = enabled,
            fieldInputErrorMsg = urlErrorMessage,
            hintTest = "Enter URL",
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onAddUrl,
            enabled = url.isNotEmpty() && urlErrorMessage == null && enabled,
            modifier = Modifier.weight(0.2f)
        ) {
            Text("Add URL")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onImport, enabled = enabled, modifier = Modifier.weight(0.2f)) {
            Text("Import..")
        }
    }
}
