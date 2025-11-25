package com.github.huymaster.textguardian.server.net

import com.github.huymaster.textguardian.server.api.APIVersion1
import com.github.huymaster.textguardian.server.api.BaseAPI
import com.github.huymaster.textguardian.server.api.TestAPI
import com.github.huymaster.textguardian.server.logCallException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream

private val apiVersions = mutableListOf<Int>()
private val exceptionRoot = File("exceptions")
private val classLoader = Thread.currentThread().contextClassLoader
private val typeMap = mapOf(
    "html" to ContentType.Text.Html,
    "css" to ContentType.Text.CSS,
    "js" to ContentType.Application.JavaScript
)

private suspend fun resourceScanner(call: ApplicationCall) {
    val path = call.request.path().removePrefix("/").removeSuffix("/")
    val stream: InputStream? = classLoader.getResourceAsStream("www/${path}")
    if (stream != null)
        call.respondText(
            contentType = typeMap[path.substringAfterLast(".")],
            text = stream.bufferedReader().readText()
        )
}

fun Application.configureRouting() {
    routing {
        get("/") { call.respondRedirect("/index.html") }
        get(Regex(".*\\.(html|css|js)")) { resourceScanner(call) }
        get("/download") {}
        get("/robots.txt") {
            call.respondText(
                """
                User-agent: *
                Disallow: /
            """.trimIndent()
            )
        }
        get("/favicon.ico") {
            call.response.header(HttpHeaders.CacheControl, "max-age=31536000")
            call.respondText("OK")
        }
        registerAPI(TestAPI, this)
        registerAPI(APIVersion1, this)
        configureHoneypot()
    }
    install(StatusPages) {
        exception(Throwable::class) { call, cause ->
            val traceId = logCallException(call, cause)
            LoggerFactory.getLogger("Routing")
                .warn("An exception was written to $traceId")
            call.respondText(
                "Internal Server Error. Please contact developer with Trace ID: $traceId\nMaintainer: HuyMaster <huymastertx88@gmail.com>",
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