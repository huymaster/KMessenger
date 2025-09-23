package com.github.huymaster.textguardian.server.net

import com.github.huymaster.textguardian.core.utils.initObjectMapper
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson { initObjectMapper(this) }
    }
}