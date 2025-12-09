package com.github.huymaster.textguardian.web

import App
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
suspend fun main() {
    ComposeViewport {
        App()
    }
    val loader = document.getElementById("app-loader") as? HTMLElement
    loader?.style?.opacity = "0"

    window.setTimeout({
        loader?.remove()
        return@setTimeout null
    }, 500)
}