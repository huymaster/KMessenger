package com.github.huymaster.textguardian.server

import com.github.huymaster.textguardian.server.net.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(
        factory = Netty,
        configure = { environment() },
        module = Application::module
    ).start(wait = true)
}


fun ApplicationEngine.Configuration.environment() {
    connector {
        host = "0.0.0.0"
        port = 8080
    }
    configureHTTPS()
}

fun Application.module() {
    install(SecureLayer)
    configureDependencyInject()
    configureAdministration()
    configureSecurity()
    configureSerialization()
    configureStaticSites()
    configureHTTP()
    configureRouting()
}