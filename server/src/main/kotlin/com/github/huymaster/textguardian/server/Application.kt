package com.github.huymaster.textguardian.server

import com.github.huymaster.textguardian.server.net.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080) { module() }.start(true)
}

fun Application.module() {
    configureDependencyInject()
    configureSerialization()
    configureAuthentication()
    configureSecurity()
    configureHTTP()
    configureRouting()
}