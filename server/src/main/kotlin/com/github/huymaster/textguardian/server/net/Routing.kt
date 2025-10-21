package com.github.huymaster.textguardian.server.net

import com.github.huymaster.textguardian.server.api.APIVersion1
import com.github.huymaster.textguardian.server.api.BaseAPI
import com.github.huymaster.textguardian.server.api.TestAPI
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.PrintStream
import java.util.*

private val apiVersions = mutableListOf<Int>()
private val exceptionRoot = File("exceptions")

private suspend fun logException(call: ApplicationCall, cause: Throwable): String {
    if (!exceptionRoot.exists())
        exceptionRoot.mkdir()
    val traceId = UUID.randomUUID().toString()
    val file = File(exceptionRoot, "$traceId.log")
    val stream = PrintStream(file.outputStream())
    val request = call.request
    stream.println("Path: ${request.path()}")
    stream.println("Method: ${request.httpMethod}")
    stream.println("Header: ${request.headers.toMap().map { "${it.key}: ${it.value}" }.joinToString("\n\t")}")
    stream.println("Body: ")
    stream.println(runCatching { call.receiveStream().readBytes().decodeToString() }.getOrNull() ?: "null or consumed")
    stream.println()
    cause.printStackTrace(stream)
    stream.close()
    return traceId
}

fun Application.configureRouting() {
    routing {
        get("/") { call.respondRedirect("/api") }
        runCatching {
            swaggerUI("/api", swaggerFile = "api/openapi.yaml")
        }
        get("/health") { call.respondText("OK") }
        get("/favicon.ico") { call.respondText("OK") }
        registerAPI(TestAPI, this)
        registerAPI(APIVersion1, this)
    }
    install(StatusPages) {
        exception(Throwable::class) { call, cause ->
            val traceId = logException(call, cause)
            LoggerFactory.getLogger("Routing")
                .warn("An exception was written to $traceId")
            call.respondText(
                "Internal Server Error. Please contact developer with Trace ID: $traceId",
                status = HttpStatusCode.InternalServerError
            )
        }
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