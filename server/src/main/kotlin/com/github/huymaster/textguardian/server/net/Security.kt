package com.github.huymaster.textguardian.server.net

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    install(SecureLayer)
    authentication {
    }
}

val allowAgents = listOf(
    "KMessenger",
    "KMessenger-Debug"
)

private val notSupported = """
<!DOCTYPE html>
<html style="height: 100vh; display: flex; flex-direction: column;">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#fff">
    <title>Not Supported</title>
</head>
<body style="text-align: center; flex-grow: 1; display: flex; flex-direction: column;">
    <center style="flex-grow: 1;"><h1 style="font-family: monospace;">INVALID REQUEST</h1></center>
    <center><p>running as <span style="font-family: monospace;">root</span></p></center>
</body>
</html>
""".trimIndent()

val SecureLayer = createApplicationPlugin("SecureLayer") {
    onCall { call ->
        val agent = call.request.headers["User-Agent"]
        if (allowAgents.none { it.equals(agent, true) })
            call.respondText(notSupported, status = HttpStatusCode.Forbidden, contentType = ContentType.Text.Html)
    }
}