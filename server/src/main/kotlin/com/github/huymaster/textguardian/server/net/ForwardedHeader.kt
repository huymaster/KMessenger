package com.github.huymaster.textguardian.server.net

import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.*

fun Application.configureForwardedHeader() {
    install(XForwardedHeaders)
}