@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.github.huymaster.textguardian.jvm

import App
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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
    var dSize by remember { mutableStateOf(DpSize.Unspecified) }
    val density = LocalDensity.current
    var exitConfirmDialog by remember { mutableStateOf(false) }
    if (exitConfirmDialog) {
        DialogWindow(
            onCloseRequest = { exitConfirmDialog = false },
            state = rememberDialogState(size = dSize),
            resizable = false,
            transparent = true,
            undecorated = true
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize(unbounded = true)
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.medium)
                    .onSizeChanged { with(density) { dSize = DpSize(it.width.toDp(), it.height.toDp()) } }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Do you want to exit?")
                    Row(
                        modifier = Modifier.padding(start = 64.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(
                            { exitConfirmDialog = false }
                        ) { Text("No") }
                        Button(::exitApplication) {
                            Text("Yes")
                        }
                    }
                }
            }
        }
    } else
        Window(
            state = state,
            title = "KMessenger",
            onCloseRequest = { exitConfirmDialog = true }
        ) {
            MaterialTheme {
                Scaffold {
                    Surface(
                        modifier = Modifier.padding(it)
                    ) {
                        App()
                    }
                }
            }
        }
}