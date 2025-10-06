package com.github.huymaster.textguardian.server.api

import com.github.huymaster.textguardian.server.api.v1.AuthenticationAction
import com.github.huymaster.textguardian.server.data.repository.CredentialRepository
import com.github.huymaster.textguardian.server.data.repository.UserRepository
import com.github.huymaster.textguardian.server.data.repository.UserTokenRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.inject

object APIVersion1 : BaseAPI(1) {

    private val userRepository: UserRepository by inject()
    private val credentialRepository: CredentialRepository by inject()
    private val userTokenRepository: UserTokenRepository by inject()

    override fun Route.register() {
        registerAuthenticationActions()
    }

    private fun Route.registerAuthenticationActions() {
        post("/register") {
            AuthenticationAction.registerAction(
                call,
                call.receive(),
                database,
                userRepository,
                credentialRepository
            )
        }
        post("/login") {
            AuthenticationAction.loginAction(
                call,
                call.receive(),
                userRepository,
                credentialRepository,
                userTokenRepository
            )
        }
        post("/refresh") { AuthenticationAction.refreshAction(call, call.receive(), userTokenRepository) }
        post("/checkSession") { AuthenticationAction.checkSessionAction(call, call.receive(), userTokenRepository) }
        delete("/revoke") { AuthenticationAction.revokeToken(call, call.receive(), userTokenRepository) }
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