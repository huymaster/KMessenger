package com.github.huymaster.textguardian.server.api

import com.auth0.jwt.JWT
import com.github.huymaster.textguardian.core.api.type.WebSocketMsg
import com.github.huymaster.textguardian.server.api.v1.AuthRoute
import com.github.huymaster.textguardian.server.api.v1.ChatRoute
import com.github.huymaster.textguardian.server.api.v1.ConversationRoute
import com.github.huymaster.textguardian.server.api.v1.UserRoute
import com.github.huymaster.textguardian.server.data.repository.MessageRepository
import com.github.huymaster.textguardian.server.data.repository.RepositoryResult
import com.github.huymaster.textguardian.server.data.repository.UserRepository
import com.github.huymaster.textguardian.server.data.repository.UserTokenRepository
import com.github.huymaster.textguardian.server.net.ALGORITHM
import com.github.huymaster.textguardian.server.net.REFRESH_TOKEN_CLAIM
import com.github.huymaster.textguardian.server.net.USER_ID_CLAIM
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.html.*
import org.koin.core.component.inject
import org.ktorm.dsl.eq
import java.util.*

object APIVersion1 : BaseAPI(1) {
    override fun Route.register() {
        get("/openapi.yaml") {
            val stream = ClassLoader.getSystemClassLoader().getResourceAsStream("openapi.yaml") ?: return@get
            call.respondOutputStream { stream.copyTo(this) }
        }
        authRoute()
        userRoute()
        conversationRoute()
        chatRoute()
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
        protect {
            get("/auth/check") { AuthRoute.check(call) }
            delete("/auth/logout") { AuthRoute.logout(call) }
        }
    }

    private fun Route.userRoute() {
        get("/users") { UserRoute.getUsers(call) }
        get("/keys") { UserRoute.getPublicKeys(call) }
        protect {
            get("/user") { UserRoute.getUser(call) }
            put("/user") { UserRoute.updateInfo(call) }
            post("/key") { UserRoute.addPublicKey(call) }
        }
    }

    private fun Route.conversationRoute() {
        protect {
            get("/conversations") { ConversationRoute.getConversations(call) }
            get("/conversation/{conversationId}") { ConversationRoute.getConversation(call) }
            post("/conversation") { ConversationRoute.createConvertation(call) }
            put("/conversation/{conversationId}") { ConversationRoute.renameConversation(call) }
            delete("/conversation/{conversationId}") { ConversationRoute.deleteConversation(call) }
            get("/conversation/{conversationId}/participants") { ConversationRoute.getParticipants(call) }
            post("/participant") { ConversationRoute.addParticipant(call) }
            delete("/participant") { ConversationRoute.removeParticipant(call) }
        }
    }

    private fun Route.chatRoute() {
        protect {
            post("/message/{conversationId}") { ChatRoute.sendMessage(call) }
            get("/message/{conversationId}/{messageId}") { ChatRoute.getMessage(call) }
            get("/message/{conversationId}") { ChatRoute.getMessages(call) }
            get("/message/{conversationId}/latest") { ChatRoute.getLatestMessages(call) }
        }
        webSocket("/message/{conversationId}") {
            send(Frame.Text(WebSocketMsg.HANDSHAKE))

            val tokenFrame = withTimeoutOrNull(5000) { incoming.receive() }
            val token = (tokenFrame as? Frame.Text)?.readText()

            if (token == null || !checkToken(token)) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid Token"))
                return@webSocket
            }

            val conversationId = call.parameters["conversationId"]
            if (conversationId.isNullOrBlank()) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing ID"))
                return@webSocket
            }

            try {
                val repo: MessageRepository by inject()
                repo.newMsgFlow.collect { msgConvId ->
                    if (msgConvId == conversationId) {
                        send(Frame.Text(WebSocketMsg.NEW_MESSAGE))
                    }
                }
            } catch (e: Exception) {
            } finally {
            }
        }
    }

    private suspend fun checkToken(token: String): Boolean {
        val o = JWT.require(ALGORITHM).build().verify(token)
        val userId = runCatching {
            UUID.fromString(o.getClaim(USER_ID_CLAIM)?.asString())
        }.getOrNull() ?: return false
        val refreshToken = runCatching {
            o.getClaim(REFRESH_TOKEN_CLAIM)?.asString()
        }.getOrNull() ?: return false

        val uRep by inject<UserRepository>()
        val tRep by inject<UserTokenRepository>()

        val userExists = uRep.exists { e -> e.userId eq userId }
        if (!userExists) return false

        val tokenResult = tRep.checkToken(refreshToken)
        return tokenResult !is RepositoryResult.Error
    }
}