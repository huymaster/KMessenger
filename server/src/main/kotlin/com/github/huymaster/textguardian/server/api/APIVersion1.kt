package com.github.huymaster.textguardian.server.api

import com.github.huymaster.textguardian.server.api.v1.AuthRoute
import com.github.huymaster.textguardian.server.api.v1.ConversationRoute
import com.github.huymaster.textguardian.server.api.v1.UserRoute
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

object APIVersion1 : BaseAPI(1) {
    override fun Route.register() {
        get("/openapi.yaml") {
            val stream = ClassLoader.getSystemClassLoader().getResourceAsStream("openapi.yaml") ?: return@get
            call.respondOutputStream { stream.copyTo(this) }
        }
        authRoute()
        userRoute()
        conversationRoute()
    }

    override suspend fun RoutingContext.swaggerProvider() {
        val fullPath = call.request.path()
        call.respondHtml {
            head {
                title { +"APIv$version" }
                meta {
                    name = "viewport"
                    content = "width=device-width, initial-scale=1.0"
                }
                link(
                    href = "https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.29.1/swagger-ui.min.css",
                    rel = LinkRel.stylesheet
                )
            }
            body {
                div { id = "swagger-ui" }
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.29.1/swagger-ui-bundle.min.js") {
                    crossorigin = ScriptCrossorigin.anonymous
                }
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.29.1/swagger-ui-standalone-preset.min.js") {
                    crossorigin = ScriptCrossorigin.anonymous
                }
                script {
                    unsafe {
                        +"""
                            window.onload = () => {
                                window.ui = SwaggerUIBundle({
                                    url: '$fullPath/openapi.yaml',
                                    dom_id: '#swagger-ui',
                                });
                            };
                            """.trimIndent()
                    }
                }
            }
        }
    }

    private fun Route.authRoute() {
        post("/auth/register") { AuthRoute.register(call) }
        post("/auth/login") { AuthRoute.login(call) }
        get("/auth/refresh") { AuthRoute.refresh(call) }
        protect { delete("/auth/logout") { AuthRoute.logout(call) } }
    }

    private fun Route.userRoute() {
        get("/users") { UserRoute.getUsers(call) }
        protect {
            get("/user") { UserRoute.getMe(call) }
            put("/user") { UserRoute.updateInfo(call) }
        }
    }

    private fun Route.conversationRoute() {
        protect {
            get("/conversations") { ConversationRoute.getConversations(call) }
            post("/conversation") { ConversationRoute.createConvertation(call) }
        }
    }
}