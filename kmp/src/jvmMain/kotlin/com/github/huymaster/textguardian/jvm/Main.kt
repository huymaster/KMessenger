@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.github.huymaster.textguardian.jvm

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.delay
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
        onCloseRequest = ::exitApplication
    ) {
        MaterialTheme {
            App()
        }
    }
}

@Composable
fun App() {
    val time by timeFlow.collectAsState()
    LaunchedEffect(time) { delay(1000); timeFlow.value = Instant.now() }
    Text(time.toString())
}