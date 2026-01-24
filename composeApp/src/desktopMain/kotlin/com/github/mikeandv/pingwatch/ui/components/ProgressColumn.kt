package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressColumn(progress: Long) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        // Background track (dimmed circle)
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 6.dp,
            color = MaterialTheme.colors.primary.copy(alpha = 0.2f)
        )
        // Foreground progress
        CircularProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 6.dp
        )
        Text(
            text = "$progress%",
            style = MaterialTheme.typography.body2
        )
    }
}
