package com.github.huymaster.textguardian.server.net

import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
}