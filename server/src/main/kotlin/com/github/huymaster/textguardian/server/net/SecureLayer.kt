package com.github.huymaster.textguardian.server.net

import io.ktor.server.application.*

val allowAgents = listOf(
    "KMessenger",
    "KMessenger-Debug"
)

val SecureLayer = createApplicationPlugin("SecureLayer") {
}