package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.domain.RunType
import com.github.mikeandv.pingwatch.domain.TestCaseParams
import com.github.mikeandv.pingwatch.domain.TestCaseSettings
import com.github.mikeandv.pingwatch.ui.handlers.handleIndividualTestCountChange
import com.github.mikeandv.pingwatch.ui.handlers.handleIndividualTimeInputChange

@Composable
fun UrlProgressIndicator(progress: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(18.dp)
    ) {
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 3.dp,
            color = MaterialTheme.colors.primary.copy(alpha = 0.2f)
        )
        CircularProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun IndividualInputField(
    settings: TestCaseSettings,
    runType: RunType,
    params: TestCaseParams,
    url: String,
    updateIndividualTime: (Long, String) -> Unit,
    updateIndividualUnformattedTime: (String, String) -> Unit,
    updateIndividualCount: (Long, String) -> Unit,
    onErrorChange: (String?) -> Unit,
    enabled: Boolean
) {
    val (value, hint, onValueChange) = if (runType == RunType.DURATION) {
        Triple(
            params.unformattedDurationValue,
            "Enter time (MM:SS)",
            { input: String ->
                handleIndividualTimeInputChange(
                    input, url, updateIndividualTime, updateIndividualUnformattedTime, onErrorChange
                )
            }
        )
    } else {
        Triple(
            if (params.countValue == 0L) "" else params.countValue.toString(),
            "Enter the number",
            { input: String ->
                handleIndividualTestCountChange(
                    input,
                    url,
                    updateIndividualCount,
                    onErrorChange,
                    settings.minCommonInput,
                    settings.maxCountInput
                )
            }
        )
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        enabled = enabled,
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, if (enabled) Color.Gray else Color.LightGray, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(text = hint, color = Color.Gray, style = MaterialTheme.typography.body2)
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun RemoveButton(onClick: () -> Unit, enabled: Boolean) {
    IconButton(onClick = onClick, modifier = Modifier.size(18.dp), enabled = enabled) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Remove",
            tint = if (enabled) Color.Gray else Color.LightGray
        )
    }
}
