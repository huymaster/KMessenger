package com.github.huymaster.textguardian.server

import com.github.huymaster.textguardian.server.net.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlin.system.exitProcess

lateinit var server: EmbeddedServer<*, *>

fun main() {
    server = embeddedServer(
        factory = Netty,
        configure = { environment() },
        module = Application::module
    )
    server.addShutdownHook { exitProcess(0) }
    server.start(true)
}


fun ApplicationEngine.Configuration.environment() {
    connector {
        host = "0.0.0.0"
        port = 8080
    }
}

fun Application.module() {
    configureSerialization()
    install(SecureLayer)
    configureDependencyInject()
    configureAdministration()
    configureSecurity()
    configureStaticSites()
    configureHTTP()
    configureRouting()
}