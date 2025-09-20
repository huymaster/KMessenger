package com.github.huymaster.textguardian.server.net

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*

val allowAgents = listOf(
    "KMessenger",
    "KMessenger-Debug"
)

val SecureLayer = createApplicationPlugin("SecureLayer") {
    onCall {
        val path = it.request.path()
        if (path == "/" || path.startsWith("/web"))
            return@onCall
        val agent = it.request.headers[HttpHeaders.UserAgent]
//        if (agent !in allowAgents)
//            it.respondText("You are not allowed to access this!", status = HttpStatusCode.Forbidden)
    }
}