package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp


@Composable
fun CommonInputField(
    input: String,
    onFieldChange: (String) -> Unit,
    enabled: Boolean,
    fieldInputErrorMsg: String?,
    hintTest: String? = null,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = " ",
            color = Color.Transparent,
            style = MaterialTheme.typography.caption
        )
        BasicTextField(
            value = input,
            onValueChange = onFieldChange,
            singleLine = true,
            enabled = enabled,
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            modifier = modifier
                .height(36.dp)
                .border(
                    1.dp,
                    when {
                        !enabled -> Color.LightGray
                        fieldInputErrorMsg != null -> MaterialTheme.colors.error
                        else -> Color.Gray
                    },
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            decorationBox = { innerTextField ->
                if (hintTest != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (input.isEmpty()) {
                            Text(
                                text = hintTest,
                                color = Color.Gray,
                                style = MaterialTheme.typography.body2
                            )
                        }
                        innerTextField()
                    }
                } else {
                    innerTextField()
                }
            }
        )
        Text(
            text = fieldInputErrorMsg ?: " ",
            color = if (fieldInputErrorMsg != null) MaterialTheme.colors.error else Color.Transparent,
            style = MaterialTheme.typography.caption,
        )
    }
}
