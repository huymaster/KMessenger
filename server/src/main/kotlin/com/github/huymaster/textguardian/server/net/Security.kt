package com.github.huymaster.textguardian.server.net

import io.ktor.server.application.*

fun Application.configureSecurity() {
    install(SecureLayer)
}

val SecureLayer = createApplicationPlugin("SecureLayer") {
}