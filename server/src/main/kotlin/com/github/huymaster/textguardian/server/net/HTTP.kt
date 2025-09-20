package com.github.huymaster.textguardian.server.net

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.httpsredirect.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.routing.*
import java.io.File
import java.security.KeyStore

val keystore = File("keystore.jks")
val cert: KeyStore? = runCatching { KeyStore.getInstance(keystore, "062425".toCharArray()) }.getOrNull()

fun Application.configureHTTP() {
    install(PartialContent) {
        maxRangeCount = 10
    }
    if (keystore.exists() && cert != null)
        install(HttpsRedirect) {
            sslPort = 8443
            permanentRedirect = false
            exclude { it.request.headers[HttpHeaders.UserAgent] == "KMessenger-Debug" }
        }
    else
        log.warn("Keystore invalid. HTTPS will not be enabled")
    install(HSTS) {
        maxAgeInSeconds = 30 * 24 * 60 * 60
        includeSubDomains = true
        preload = true
        this.filter { it.request.headers[HttpHeaders.UserAgent] != "KMessenger-Debug" }
    }
    install(IgnoreTrailingSlash)
    install(DefaultHeaders) {
        header(HttpHeaders.Server, "Ktor v3.3.0")
    }
    install(ConditionalHeaders)
    install(Compression) {
        gzip {}
        deflate {}
    }
}

fun ApplicationEngine.Configuration.configureHTTPS() {
    if (!keystore.exists() || cert == null)
        return
    sslConnector(
        cert,
        keyAlias = "KMessenger",
        keyStorePassword = { "062425".toCharArray() },
        privateKeyPassword = { "062425".toCharArray() }
    ) {
        port = 8443
    }
}