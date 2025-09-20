package com.github.huymaster.textguardian.server.net

import com.github.huymaster.textguardian.server.api.APIVersion1
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondRedirect("/web")
        }
        APIVersion1.register(this)
    }
}