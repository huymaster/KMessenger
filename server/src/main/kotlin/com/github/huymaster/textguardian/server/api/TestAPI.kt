package com.github.huymaster.textguardian.server.api

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.html.*
import java.io.File


class WebSocketLogAppender : AppenderBase<ILoggingEvent>() {
    companion object {
        private val _FLOW = MutableStateFlow("")
        val FLOW = _FLOW.asSharedFlow()
    }

    var encoder: PatternLayoutEncoder? = null

    override fun append(event: ILoggingEvent?) {
        event ?: return
        val bytes = encoder?.encode(event) ?: return
        _FLOW.update { bytes.toString(Charsets.UTF_8) }
    }

    override fun start() {
        if (encoder == null) {
            encoder = PatternLayoutEncoder()
            encoder?.pattern = "%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
        }
        encoder?.start()
        super.start()
    }

    override fun stop() {
        encoder?.stop()
        super.stop()
    }
}

object TestAPI : BaseAPI(0) {
    override fun Route.register() {
        get("/error") {
        }
        get("/logs") {
            val auth = call.request.headers["Authorization"]
            if (auth == null || auth != "Basic ${System.getenv("LOG_AUTHENTICATION")}") {
                call.response.header(HttpHeaders.WWWAuthenticate, "Basic")
                call.response.status(HttpStatusCode.Unauthorized)
                return@get
            }
            call.respondHtml {
                head {
                    script {
                        unsafe {
                            +"""
                            window.addEventListener('load', setup)
                    
                            function setup() {
                                const url = window.location.host;
                                const autoScroll = document.getElementById("autoScroll");
                                const log = document.getElementById("log");
                                const protocol = window.location.protocol === "https:" ? "wss" : "ws";
                                const callback = (list, observer) => {
                                    for (const entry of list)
                                        if (entry.type === 'childList')
                                                log.scrollTo({
                                                    left: 0,
                                                    top: log.scrollHeight,
                                                    behavior: "smooth"
                                                })
                                };
                                const observer = new MutationObserver(callback);
                                const config = {childList: true};
                                observer.observe(log, config);
                                const ws = new WebSocket(protocol + "://" + url + "/api/v0/logs");
                                ws.onmessage = function (event) {
                                    const message = event.data;
                                    log.innerHTML += message
                                };
                            }
                            """.trimIndent()
                        }
                    }
                    style {
                        unsafe {
                            +"""
                            body {
                                margin: 0;
                                padding: 0;
                                display: flex;
                                flex-direction: column;
                                justify-content: center;
                                align-items: center;
                                height: 100vh;
                                width: 100vw;
                            }
                    
                            #container {
                                display: flex;
                                flex-direction: row;
                                justify-content: center;
                                align-items: center;
                                height: 100vh;
                                width: 100vw;
                            }
                    
                            #log {
                                flex-grow: 1;
                                padding: 16px;
                                margin: 16px 10vw;
                                height: 80%;
                                max-height: 80%;
                                background-color: #161616;
                                color: #fff;
                                border-radius: 8px;
                                font-family: monospace;
                                overflow-y: auto;
                                white-space: pre-wrap;
                                tab-size: 4;
                            }
                            """.trimIndent()
                        }
                    }
                }
                body {
                    div {
                        id = "container"
                        div {
                            id = "log"
                            +File("logs/app.log").readText()
                        }
                    }
                }
            }
        }
        webSocket("/logs") {
            val auth = call.request.headers["Authorization"]
            if (auth == null || auth != "Basic ${System.getenv("LOG_AUTHENTICATION")}") {
                call.response.header(HttpHeaders.WWWAuthenticate, "Basic")
                call.response.status(HttpStatusCode.Unauthorized)
                return@webSocket
            }
            WebSocketLogAppender.FLOW.collectLatest { send(it) }
        }
    }
}