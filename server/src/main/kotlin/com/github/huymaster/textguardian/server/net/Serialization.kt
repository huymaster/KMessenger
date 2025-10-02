package com.github.huymaster.textguardian.server.net

import com.github.huymaster.textguardian.core.utils.DEFAULT_OBJECT_MAPPER
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(DEFAULT_OBJECT_MAPPER))
    }
}