package com.github.huymaster.textguardian.server.net

import com.github.huymaster.textguardian.server.runningDirectory
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.html.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.html.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicLong

private val REGEX_ANY = Regex("(/)?.+")

private const val ZIP_BOMB_THRESHOLD = 1024
private const val SPIDER_TRAP_THRESHOLD = 4096
private const val TARPIT_HOLE_THRESHOLD = 512
private const val DYNAMIC_CONTENT_THRESHOLD = 4096

private val CURRENT_ZIP_BOMB = AtomicLong(0)
private val CURRENT_SPIDER_TRAP = AtomicLong(0)
private val CURRENT_TARPIT_HOLE = AtomicLong(0)
private val CURRENT_DYNAMIC_CONTENT = AtomicLong(0)

private val LOGGER = LoggerFactory.getLogger("Honeypot")
private val trigger = File(runningDirectory, "honeypot.txt")

private enum class HoneypotType(
    val condition: () -> Boolean,
    val action: suspend (RoutingCall) -> Unit
) {
    ZIP_BOMB({ CURRENT_ZIP_BOMB.get() < ZIP_BOMB_THRESHOLD }, ::zipBomb),
    SPIDER_TRAP({ CURRENT_SPIDER_TRAP.get() < SPIDER_TRAP_THRESHOLD }, ::spiderTrap),
    TARPIT_HOLE({ CURRENT_TARPIT_HOLE.get() < TARPIT_HOLE_THRESHOLD }, ::tarpitHole),
    DYNAMIC_CONTENT({ CURRENT_DYNAMIC_CONTENT.get() < DYNAMIC_CONTENT_THRESHOLD }, ::dynamicContent)
}

fun Routing.configureHoneypot() {
    if (!trigger.exists()) trigger.createNewFile()
    get(REGEX_ANY) { methodChooser(call) }
    post(REGEX_ANY) { methodChooser(call) }
    put(REGEX_ANY) { methodChooser(call) }
    delete(REGEX_ANY) { methodChooser(call) }
    patch(REGEX_ANY) { methodChooser(call) }
    options(REGEX_ANY) { methodChooser(call) }
}

private suspend fun methodChooser(call: RoutingCall) {
    do {
        val type = HoneypotType.entries.random()
        if (type.condition()) {
            type.action(call)
            LOGGER.info("[${call.request.origin.remoteAddress}] ${type.name}")
            break
        }
        delay(500)
    } while (true)
}

private suspend fun zipBomb(call: RoutingCall) {
    CURRENT_ZIP_BOMB.incrementAndGet()
    try {
        val inStream = ClassLoader.getSystemClassLoader().getResourceAsStream("bomb.zip") ?: return
        val content: ByteArray = inStream.readBytes()
        runCatching {
            call.response.header(HttpHeaders.ContentLength, content.size)
            call.response.header(HttpHeaders.ContentType, "application/zip")
            call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"logs.zip\"")
        }
        call.respond(content)
    } finally {
        CURRENT_ZIP_BOMB.decrementAndGet()
    }
}

private suspend fun tarpitHole(call: RoutingCall) {
    CURRENT_TARPIT_HOLE.incrementAndGet()
    try {
        val messages = listOf(
            "Checking...",
            "Almost there...",
            "Analyzing request...",
            "Just a moment...",
            "Almost done...",
            "A little more..."
        )
        call.respond(object : OutgoingContent.WriteChannelContent() {
            override suspend fun writeTo(channel: ByteWriteChannel) {
                runCatching {
                    while (true) {
                        channel.writeStringUtf8("${messages.random()}\n")
                        channel.flush()
                        delay(5000)
                    }
                }.onFailure {
                    channel.flushAndClose()
                }
            }
        })
    } finally {
        CURRENT_TARPIT_HOLE.decrementAndGet()
    }
}

private suspend fun spiderTrap(call: RoutingCall) {
    CURRENT_SPIDER_TRAP.incrementAndGet()
    try {
        call.respondHtml {
            head {}
            body {
                ul {
                    for (i in 1..100)
                        li { a(href = "${call.request.path()}/page/${UUID.randomUUID()}") { +"Page $i" } }
                }
            }
        }
    } finally {
        CURRENT_SPIDER_TRAP.decrementAndGet()
    }
}

private suspend fun dynamicContent(call: RoutingCall) {
    CURRENT_DYNAMIC_CONTENT.incrementAndGet()
    try {
        call.respondHtml {
            head {}
            body {
                p { +UUID.randomUUID().toString() }
                p { +"Time: ${System.currentTimeMillis()}" }
            }
        }
    } finally {
        CURRENT_DYNAMIC_CONTENT.decrementAndGet()
    }
}