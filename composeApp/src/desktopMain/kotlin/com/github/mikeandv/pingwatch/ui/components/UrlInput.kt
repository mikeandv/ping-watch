package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = url,
            onValueChange = onUrlChange,
            singleLine = true,
            enabled = enabled,
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .border(1.dp, if (enabled) Color.Gray else Color.LightGray, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (url.isEmpty()) {
                        Text(
                            text = "Enter URL",
                            color = Color.Gray,
                            style = MaterialTheme.typography.body2
                        )
                    }
                    innerTextField()
                }
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onAddUrl, enabled = url.isNotEmpty() && urlErrorMessage == null && enabled) {
            Text("Add URL")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onImport, enabled = enabled) {
            Text("Import..")
        }
    }
}
