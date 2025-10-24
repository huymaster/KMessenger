package com.github.huymaster.textguardian.server.net

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Application.configureHTTP() {
    install(PartialContent) {
        maxRangeCount = 64
    }
    install(HSTS) {
        maxAgeInSeconds = 3600
        includeSubDomains = true
        this.filter { it.request.headers[HttpHeaders.UserAgent] != "KMessenger-Debug" }
    }
    install(WebSockets) {
        pingPeriodMillis = 5000
        timeoutMillis = 5000
        maxFrameSize = 1024 * 1024 * 1024
    }
    install(IgnoreTrailingSlash)
    install(CORS) {
        anyHost()
        anyMethod()
        allowHeader(HttpHeaders.ContentType)
    }
    install(DefaultHeaders) {
        header(HttpHeaders.Server, "Ktor v3.3.0")
    }
    install(ConditionalHeaders)
    install(Compression) {
        gzip {}
        deflate {}
    }
}