package com.github.huymaster.textguardian.server.net

import com.github.huymaster.textguardian.server.api.APIVersion1
import com.github.huymaster.textguardian.server.api.BaseAPI
import com.github.huymaster.textguardian.server.api.TestAPI
import com.github.huymaster.textguardian.server.logCallException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import org.slf4j.LoggerFactory
import java.io.File

private val apiVersions = mutableListOf<Int>()
private val exceptionRoot = File("exceptions")

fun Application.configureRouting() {
    routing {
        get("/") {
            val fullPath = call.request.path()
            println(fullPath)
            call.respondHtml {
                head {
                    title { +"KMessenger" }
                    style {
                        unsafe {
                            +"ul { list-style-type: none; margin: 0; padding: 0; display: flex; flex-direction: column; }"
                            +"li { margin: 10px; }"
                            +"a { text-decoration: none; display: inline-block; margin: 2px; padding: 10px; border: 1px solid #ccc; border-radius: 5px; transition: background-color 0.3s; }"
                            +"a:hover { background-color: #f0f0f0; }"
                        }
                    }
                }
                body {
                    h1 { +"API Documentations" }
                    ul {
                        for (api in BaseAPI.API_LIST) {
                            li {
                                a(href = "/${api.value}") { +"API version ${api.key}" }
                            }
                        }
                    }
                }
            }
        }
        get("/favicon.ico") { call.respondText("OK") }
        registerAPI(TestAPI, this)
        registerAPI(APIVersion1, this)
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