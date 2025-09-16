package com.github.huymaster.textguardian.server.net

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondRedirect("/web")
        }
        get("/api/{any}") {
            val addition = call.parameters["any"]?.toCharArray()?.sumOf { ch -> ch.code } ?: 0
            call.respondText(
                String.format("%016x", System.currentTimeMillis() + addition),
                status = HttpStatusCode.OK
            )
        }
    }
}