package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.utils.checkIsNotRunningStatus
import com.github.mikeandv.pingwatch.domain.TestCaseState
import com.github.mikeandv.pingwatch.domain.StatusCode
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun FlowControlButtons(
    testCaseState: TestCaseState,
    onLaunchTest: () -> Unit,
    onNavigate: () -> Unit,
    cancelFlag: AtomicBoolean,
    updateShowDialog: (Boolean) -> Unit,
    updateDialogMessage: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val status by testCaseState.status.collectAsState()
    val isRunning = status == StatusCode.RUNNING
    val isNotRunning = checkIsNotRunningStatus(status)
    val canViewResult = status == StatusCode.FINISHED

    Row {
        LaunchButton(enabled = isNotRunning, onClick = onLaunchTest)
        Spacer(modifier = Modifier.width(8.dp))
        CancelButton(enabled = isRunning) {
            cancelFlag.set(true)
            updateDialogMessage("Test canceled by user!")
            updateShowDialog(true)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onNavigate, enabled = canViewResult) {
            Text("Get Result")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onSettingsClick, enabled = isNotRunning) {
            Text("Settings")
        }
    }
}

@Composable
private fun LaunchButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF41C300)),
        onClick = onClick,
        enabled = enabled
    ) {
        Text("Launch")
    }
}

@Composable
private fun CancelButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
        onClick = onClick,
        enabled = enabled
    ) {
        Text("Cancel")
    }
}
