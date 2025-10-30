package com.github.huymaster.textguardian.server.net

import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

fun Application.configureSecurity() {
    install(SecureLayer)
}

private val DISPATCHER = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
private val SCOPE = CoroutineScope(DISPATCHER)
private val LOGGER = LoggerFactory.getLogger("SecureLayer")

private enum class Direction(val arrow: String) {
    IN(">>>"), OUT("<<<"), FAIL("-X-")
}

private fun buildString(call: ApplicationCall, direction: Direction): String {
    val builder = StringBuilder()
    val ip = call.request.origin.remoteAddress
    val path = call.request.path()
    val scheme = call.request.origin.scheme
    val code = call.response.status()?.value

    builder.append("[${ip}]")
    builder.append(" ${direction.arrow} ")
    builder.append("[${scheme.padEnd(5, '-')}] ")
    if (code != null)
        builder.append("[$code] ".padEnd(6, ' '))
    else
        builder.append("[---] ")
    builder.append(path)
    return builder.toString()
}

private fun logI(string: String) = SCOPE.launch { LOGGER.info(string) }

val SecureLayer = createApplicationPlugin("SecureLayer") {
    on(CallSetup) { logI(buildString(it, Direction.IN)) }
    on(ResponseSent) { logI(buildString(it, Direction.OUT)) }
    on(CallFailed) { call, cause -> logI(buildString(call, Direction.FAIL)) }
}