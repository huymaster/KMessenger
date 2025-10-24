package com.github.huymaster.textguardian.server

import com.github.huymaster.textguardian.server.net.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.util.*
import java.io.File
import java.io.PrintStream
import java.util.*
import kotlin.system.exitProcess

val runningDirectory = File(File("").absolutePath)
private val exceptionDirectory: File
    get() {
        val f = File(runningDirectory, "exceptions")
        if (!f.exists())
            f.mkdir()
        return f
    }

fun logException(cause: Throwable, writeDirectory: File = exceptionDirectory): String {
    if (!writeDirectory.exists())
        writeDirectory.mkdir()
    val traceId = UUID.randomUUID().toString()
    val file = File(writeDirectory, "$traceId.log")
    val stream = PrintStream(file.outputStream())
    cause.printStackTrace(stream)
    stream.close()
    System.err.println("An exception was written to ${file.absolutePath}")
    return traceId
}

suspend fun logCallException(
    call: ApplicationCall,
    cause: Throwable,
    writeDirectory: File = exceptionDirectory
): String {
    if (!writeDirectory.exists())
        writeDirectory.mkdir()
    val traceId = UUID.randomUUID().toString()
    val file = File(writeDirectory, "$traceId.log")
    val stream = PrintStream(file.outputStream())
    val request = call.request
    stream.println("Remote Host: ${request.origin.remoteHost}")
    stream.println("Path: ${request.path()}")
    stream.println("Method: ${request.httpMethod}")
    stream.println("Header: ${request.headers.toMap().map { "${it.key}: ${it.value}" }.joinToString("\n\t")}")
    stream.println("Body: ")
    stream.println(
        runCatching {
            call.receiveStream().readBytes().decodeToString().ifBlank { null }
        }.getOrNull() ?: "null or consumed"
    )
    stream.println()
    cause.printStackTrace(stream)
    stream.close()
    return traceId
}

fun main() {
    runCatching {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            logException(e, exceptionDirectory)
            exitProcess(1)
        }
        embeddedServer(Netty, port = 8080) { module() }.start(true)
    }.onFailure { logException(it) }
}

fun Application.module() {
    configureForwardedHeader()
    configureRateLimit()
    configureDependencyInject()
    configureSerialization()
    configureAuthentication()
    configureSecurity()
    configureHTTP()
    configureRouting()
}