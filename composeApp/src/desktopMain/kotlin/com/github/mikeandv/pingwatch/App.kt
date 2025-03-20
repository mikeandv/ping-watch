package com.github.mikeandv.pingwatch

import androidx.compose.runtime.*
import com.github.mikeandv.pingwatch.ui.screens.MainScreen
import com.github.mikeandv.pingwatch.ui.screens.ReportScreen
import com.github.mikeandv.pingwatch.ui.viewmodels.MainScreenViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App() {
    var screen by remember { mutableStateOf("home") }
    val viewModel = remember { MainScreenViewModel() }

    when (screen) {
        "home" -> MainScreen(viewModel = viewModel, onNavigate = { screen = "report" })
        "report" -> ReportScreen(viewModel = viewModel, onNavigateBack = { screen = "home" })
    }
}

@Composable
@Preview
fun PreviewApp() {
    App()
}
