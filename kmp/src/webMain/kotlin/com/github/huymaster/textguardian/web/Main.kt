package com.github.huymaster.textguardian.web

import App
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main() {
    ComposeViewport {
        App()
    }
}