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
import kotlin.time.Duration.Companion.seconds

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
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    install(IgnoreTrailingSlash)
    install(CORS) {
        anyHost()
        anyMethod()
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