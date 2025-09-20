package com.github.huymaster.textguardian.server.net

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

private val webroot = File("webroot")
private val htmlroot = File("webroot/html")
private val htmlExtensions = listOf("html", "htm")
private val cssroot = File("webroot/css")
private val cssExtensions = listOf("css")
private val jsroot = File("webroot/js")
private val jsExtensions = listOf("js")
private val list = listOf(htmlroot, cssroot, jsroot)

fun Application.configureStaticSites() {
    if (!webroot.exists())
        webroot.mkdir()
    list.filter { !it.exists() }
        .forEach { it.mkdir() }
    routing {
        get("/web") { resolveFile(call, "index.html") }
        get("/{filename}") { resolveFile(call, call.parameters["filename"]) }
        get("/web/{filename}") { resolveFile(call, call.parameters["filename"]) }
    }
}

private suspend fun resolveFile(call: RoutingCall, filename: String?) {
    if (filename == null) {
        call.respondText(text = "File not found", status = HttpStatusCode.NotFound)
        return
    }
    if (htmlExtensions.any { filename.endsWith(it, true) })
        resolveHtml(call, filename)
    else if (cssExtensions.any { filename.endsWith(it, true) })
        resolveCSS(call, filename)
    else if (jsExtensions.any { filename.endsWith(it, true) })
        resolveJS(call, filename)
    else {
        call.respondText(text = "File not supported", status = HttpStatusCode.BadRequest)
        return
    }
}

private suspend fun resolveHtml(call: RoutingCall, filename: String) {
    if (htmlExtensions.none { filename.endsWith(it, true) }) {
        call.respondText(text = "Extension not supported", status = HttpStatusCode.BadRequest)
        return
    }
    val file = htmlroot.resolve(filename)
    if (!file.exists()) {
        call.respondText(text = "File not found", status = HttpStatusCode.NotFound)
        return
    }
    call.respondFile(file)
}

private suspend fun resolveCSS(call: RoutingCall, filename: String) {
    if (cssExtensions.none { filename.endsWith(it, true) }) {
        call.respondText(text = "Extension not supported", status = HttpStatusCode.BadRequest)
        return
    }
    val file = cssroot.resolve(filename)
    if (!file.exists()) {
        call.respondText(text = "File not found", status = HttpStatusCode.NotFound)
        return
    }
    call.respondFile(file)
}

private suspend fun resolveJS(call: RoutingCall, filename: String) {
    if (jsExtensions.none { filename.endsWith(it, true) }) {
        call.respondText(text = "Extension not supported", status = HttpStatusCode.BadRequest)
        return
    }
    val file = jsroot.resolve(filename)
    if (!file.exists()) {
        call.respondText(text = "File not found", status = HttpStatusCode.NotFound)
        return
    }
    call.respondFile(file)
}