package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun UrlCountProgress(progress: Long) {
    Text(
        text = progress.toString(),
        color = Color.Gray,
        style = MaterialTheme.typography.body2
    )
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
    onErrorChange: (String, String?) -> Unit,
    individualErrorMsg: String?,
    enabled: Boolean
) {
    val (value, hint, onValueChange) = if (runType == RunType.DURATION) {
        Triple(
            params.unformattedDurationValue,
            "Enter time (MM:SS)"
        ) { input: String ->
            handleIndividualTimeInputChange(
                input, url, updateIndividualTime, updateIndividualUnformattedTime, onErrorChange
            )
        }
    } else {
        Triple(
            if (params.countValue == 0L) "" else params.countValue.toString(),
            "Enter the number"
        ) { input: String ->
            handleIndividualTestCountChange(
                input,
                url,
                updateIndividualCount,
                onErrorChange,
                settings.minCommonInput,
                settings.maxCountInput
            )
        }
    }

    CommonInputField(
        input = value,
        onFieldChange = onValueChange,
        enabled = enabled,
        fieldInputErrorMsg = individualErrorMsg,
        hintTest = hint,
        Modifier.width(150.dp)
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
