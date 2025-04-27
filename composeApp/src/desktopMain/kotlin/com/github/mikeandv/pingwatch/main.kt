package com.github.mikeandv.pingwatch

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PingWatch",
        state = rememberWindowState(width = 800.dp, height = 800.dp)
    ) {
        App()
    }
}