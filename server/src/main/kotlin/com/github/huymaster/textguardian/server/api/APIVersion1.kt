package com.github.huymaster.textguardian.server.api

import com.github.huymaster.textguardian.core.api.type.AccessToken
import com.github.huymaster.textguardian.core.api.type.LoginRequest
import com.github.huymaster.textguardian.core.api.type.RefreshToken
import com.github.huymaster.textguardian.core.api.type.RegisterRequest
import com.github.huymaster.textguardian.core.dto.UserDTO
import com.github.huymaster.textguardian.server.data.repository.CredentialRepository
import com.github.huymaster.textguardian.server.data.repository.UserRepository
import com.github.huymaster.textguardian.server.data.repository.UserTokenRepository
import com.github.huymaster.textguardian.server.net.AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.inject
import java.util.*

object APIVersion1 : BaseAPI(1) {

    private val userRepository: UserRepository by inject()
    private val credentialRepository: CredentialRepository by inject()
    private val userTokenRepository: UserTokenRepository by inject()

    override fun Route.register() {
        post("/register") { registerAction(call, call.receive()) }
        post("/login") { loginAction(call, call.receive()) }
        post("/refresh") { refreshAction(call, call.receive()) }
        authenticate(AUTH_NAME) {
            get("/me") {
                val user = call.principal<JWTPrincipal>()
                if (user == null) {
                    sendErrorResponse(call, "Unauthorized", status = HttpStatusCode.Unauthorized)
                    return@get
                }
                val uid = user.payload.getClaim(UserDTO.ID_FIELD).asString()
                val ue = userRepository.findUserById(UUID.fromString(uid))
                call.respond(UserDTO(ue))
            }
        }
    }

    private suspend fun registerAction(call: RoutingCall, request: RegisterRequest) {
        val user = userRepository.findUserByPhoneNumber(request.phoneNumber)
        if (user != null) {
            sendErrorResponse(call, "Phone number already used")
            return
        }
        database.useTransaction {
            val newUserResult = runCatching { userRepository.newUser(request.phoneNumber) }
            val user = newUserResult.getOrNull()
            if (newUserResult.isFailure || user == null) {
                sendErrorResponse(call, "Failed to create user", newUserResult.exceptionOrNull())
                it.rollback()
                return
            }
            val newCredentialResult = runCatching { credentialRepository.createCredential(user, request.password) }
            val credential = newCredentialResult.getOrNull()
            if (newCredentialResult.isFailure || credential == null) {
                sendErrorResponse(call, "Failed to create credential", newCredentialResult.exceptionOrNull())
                it.rollback()
                return
            }
            it.commit()
            call.respondText("Registered successfully", status = HttpStatusCode.OK)
        }
    }

    private suspend fun loginAction(call: RoutingCall, request: LoginRequest) {
        val user = userRepository.findUserByPhoneNumber(request.phoneNumber)
        if (user == null) {
            sendErrorResponse(call, "User not found", status = HttpStatusCode.NotFound)
            return
        }
        val credential = credentialRepository.checkCredential(user, request.password)
        if (!credential) {
            sendErrorResponse(call, "Wrong password", status = HttpStatusCode.Unauthorized)
            return
        }
        val tokenResult = runCatching { userTokenRepository.createRefreshToken(user) }
        val token = tokenResult.getOrNull()
        if (tokenResult.isFailure || token == null) {
            sendErrorResponse(call, "Failed to create token", tokenResult.exceptionOrNull())
            return
        }
        call.respond(RefreshToken(token.refreshToken))
    }

    private suspend fun refreshAction(call: RoutingCall, request: RefreshToken) {
        val tokenResult = runCatching { userTokenRepository.validateToken(request.refreshToken) }
        if (tokenResult.isFailure || tokenResult.getOrNull() != true) {
            sendErrorResponse(
                call,
                "Invalid token",
                tokenResult.exceptionOrNull(),
                status = HttpStatusCode.Unauthorized
            )
            return
        }
        val accessToken = userTokenRepository.createAccessToken(request.refreshToken)
        call.respond(AccessToken(accessToken))
    }

    private suspend fun sendErrorResponse(
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