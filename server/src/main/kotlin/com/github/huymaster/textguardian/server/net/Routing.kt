package com.github.huymaster.textguardian.server.net

import com.github.huymaster.textguardian.server.api.APIVersion1
import com.github.huymaster.textguardian.server.api.BaseAPI
import com.github.huymaster.textguardian.server.api.TestAPI
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val apiVersions = mutableListOf<Int>()

fun Application.configureRouting() {
    routing {
        get("/") { call.respond(HttpStatusCode.BadRequest) }
        get("/health") { call.respondText("OK") }
        get("/favicon.ico") { call.respondText("OK") }
        registerAPI(TestAPI, this)
        registerAPI(APIVersion1, this)
    }
}

private fun Application.registerAPI(base: BaseAPI, routing: Routing) {
    if (apiVersions.contains(base.version))
        log.warn("API version ${base.version} is already registered")
    else {
        apiVersions.add(base.version)
        base.register(routing)
    }
}