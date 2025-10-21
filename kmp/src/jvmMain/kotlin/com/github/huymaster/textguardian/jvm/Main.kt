@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.github.huymaster.textguardian.jvm

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

val timeFlow = MutableStateFlow(Instant.now())

fun main() = application {
    val state = rememberWindowState()
    state.size = DpSize(320.dp, 240.dp)
    state.position = WindowPosition.Aligned(Alignment.Center)
    state.placement = WindowPlacement.Floating
    Window(
        state = state,
        title = "KMessenger",
        alwaysOnTop = true,
        onCloseRequest = ::exitApplication
    ) {
        MaterialTheme {
            App()
        }
    }
}

@Composable
fun App() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "OS: ${System.getProperty("os.name")}\n" +
                    "Version: ${System.getProperty("os.version")}\n" +
                    "Arch: ${System.getProperty("os.arch")}"
        )
    }
}