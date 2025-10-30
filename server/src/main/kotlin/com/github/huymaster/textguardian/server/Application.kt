package com.github.huymaster.textguardian.server

import com.github.huymaster.textguardian.server.net.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardWatchEventKinds
import java.util.*
import java.util.concurrent.TimeUnit
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

private fun stopListening(onStop: () -> Unit) {
    CoroutineScope(Job()).launch {
        val fs = FileSystems.getDefault()
        val watcher = fs.newWatchService()
        val path = runningDirectory.toPath()
        path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE)
        loop@ do {
            val key = watcher.take()
            key.pollEvents().forEach {
                if (it.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    val file = File(runningDirectory, it.context().toString())
                    if (file.name == "stop") {
                        onStop()
                        file.delete()
                        return@launch
                    }
                }
            }
            key.reset()
        } while (true)
    }
}

fun main() {
    runCatching {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            logException(e, exceptionDirectory)
            exitProcess(1)
        }
        File(runningDirectory, "logs").takeIf { it.exists() }
            ?.let { file ->
                Files.list(file.toPath())
                    .filter { it.toFile().isFile }
                    .forEach { Files.delete(it) }
            }
        File(runningDirectory, "stop").takeIf { it.exists() }?.delete()
        val server = embeddedServer(Netty, port = 8080) { module() }
        stopListening { server.stop(5, 30, TimeUnit.SECONDS) }
        server.start(true)
    }.onFailure { logException(it) }
}

fun Application.module() {
    configureForwardedHeader()
    configureSecurity()
    configureRateLimit()
    configureDependencyInject()
    configureSerialization()
    configureAuthentication()
    configureHTTP()
    configureRouting()
}