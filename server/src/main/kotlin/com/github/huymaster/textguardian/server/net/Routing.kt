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
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

private val apiVersions = mutableListOf<Int>()
private val exceptionRoot = File("exceptions")
private val classLoader = Thread.currentThread().contextClassLoader
private val typeMap = mapOf(
    "html" to ContentType.Text.Html,
    "css" to ContentType.Text.CSS,
    "js" to ContentType.Application.JavaScript,
    "wasm" to ContentType.Application.Wasm,
    "map" to ContentType.Application.Json,
    "txt" to ContentType.Text.Plain
)

private suspend fun resourceScanner(call: ApplicationCall) {
    val path = call.request.path().removePrefix("/").removeSuffix("/")
    respondRes(call, path)
}

private val etagCache = ConcurrentHashMap<String, String>()
private suspend fun respondRes(call: ApplicationCall, file: String) {
    val url = object {}.javaClass.classLoader.getResource("www/${file}")
        ?: object {}.javaClass.classLoader.getResource("web/${file}")

    if (url != null) {
        val bytes = url.readBytes()

        val etag = etagCache.computeIfAbsent(file) {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(bytes)
            hash.joinToString("") { "%02x".format(it) }
        }

        val incomingEtag = call.request.header(HttpHeaders.IfNoneMatch)
        if (incomingEtag == etag || incomingEtag == "\"$etag\"") {
            call.respond(HttpStatusCode.NotModified)
            return
        }

        val extension = file.substringAfterLast(".", "")
        val type = typeMap[extension] ?: ContentType.Application.OctetStream

        val cacheControl = when (type) {
            else -> "no-cache"
        }

        call.response.header(HttpHeaders.ETag, "\"$etag\"")
        call.response.header(HttpHeaders.CacheControl, cacheControl)
        call.response.header(HttpHeaders.ContentLength, bytes.size.toString())
        call.respondBytes(bytes, type)
    } else {
        call.respond(HttpStatusCode.NotFound)
    }
}

fun Application.configureRouting() {
    routing {
        get("/") { call.respondRedirect("/index.html") }
        get(Regex(".*\\.(${typeMap.keys.joinToString("|")})")) {
            resourceScanner(call)
        }
        get("/web") { respondRes(call, "kmp.html") }
        get("/downloadApk") {
            call.response.header(
                HttpHeaders.ContentDisposition,
                "attachment; filename=KMessenger.apk"
            )
            val stream = classLoader.getResourceAsStream("android-release.apk")
            if (stream != null)
                call.respondBytes(stream.readBytes())
            else
                call.respond(HttpStatusCode.NotFound)
        }
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