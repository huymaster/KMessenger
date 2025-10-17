package com.github.huymaster.textguardian.server.api

import com.github.huymaster.textguardian.server.api.v1.AuthenticationController
import com.github.huymaster.textguardian.server.api.v1.ConversationController
import com.github.huymaster.textguardian.server.data.repository.*
import com.github.huymaster.textguardian.server.net.AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.inject

object APIVersion1 : BaseAPI(1) {
    private val userRepository: UserRepository by inject()
    private val credentialRepository: CredentialRepository by inject()
    private val userTokenRepository: UserTokenRepository by inject()
    private val conversationRepository: ConversationRepository by inject()
    private val participantRepository: ParticipantRepository by inject()

    override fun Route.register() {
        registerAuthenticationController()
        registerConversationController()
    }

    private fun Route.registerAuthenticationController() {
        post("/register") {
            AuthenticationController.registerAction(
                call,
                call.receive(),
                database,
                userRepository,
                credentialRepository
            )
        }
        post("/login") {
            AuthenticationController.loginAction(
                call,
                call.receive(),
                userRepository,
                credentialRepository,
                userTokenRepository
            )
        }
        post("/checkSession") { AuthenticationController.checkSessionAction(call, call.receive(), userTokenRepository) }
        post("/refresh") { AuthenticationController.refreshAction(call, call.receive(), userTokenRepository) }
        authenticate(AUTH_NAME) {
            delete("/revoke") {
                AuthenticationController.revokeToken(
                    call,
                    call.receive(),
                    userTokenRepository
                )
            }
        }
    }

    private fun Route.registerConversationController() {
        authenticate(AUTH_NAME) {
            get("/conversations") {
                ConversationController.getConversationList(call, conversationRepository, participantRepository)
            }
            get("/conversation") {
                ConversationController.getConversationById(call, conversationRepository)
            }
            get("/conversation/name") {
                ConversationController.getConversationName(call, conversationRepository)
            }
        }
    }

    suspend fun sendErrorResponse(
        call: RoutingCall,
        message: String,
        exception: Throwable? = null,
        status: HttpStatusCode = HttpStatusCode.BadRequest
    ) {
        val exceptionMessage = exception?.message
        if (exceptionMessage != null)
            call.respondText("$message: $exceptionMessage", status = status)
        else
            call.respondText(message, status = status)
    }
}