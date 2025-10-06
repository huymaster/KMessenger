package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.core.api.type.AccessToken
import com.github.huymaster.textguardian.core.api.type.LoginRequest
import com.github.huymaster.textguardian.core.api.type.RefreshToken
import com.github.huymaster.textguardian.core.api.type.RegisterRequest
import com.github.huymaster.textguardian.server.api.APIVersion1.sendErrorResponse
import com.github.huymaster.textguardian.server.data.repository.CredentialRepository
import com.github.huymaster.textguardian.server.data.repository.UserRepository
import com.github.huymaster.textguardian.server.data.repository.UserTokenRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.database.Database

object AuthenticationAction {
    suspend fun registerAction(
        call: RoutingCall,
        request: RegisterRequest,
        database: Database,
        userRepository: UserRepository,
        credentialRepository: CredentialRepository
    ) {
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

    suspend fun loginAction(
        call: RoutingCall,
        request: LoginRequest,
        userRepository: UserRepository,
        credentialRepository: CredentialRepository,
        userTokenRepository: UserTokenRepository
    ) {
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

    suspend fun refreshAction(call: RoutingCall, request: RefreshToken, userTokenRepository: UserTokenRepository) {
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

    suspend fun checkSessionAction(call: RoutingCall, request: RefreshToken, userTokenRepository: UserTokenRepository) {
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
        call.respondText("Session is valid", status = HttpStatusCode.OK)
    }

    suspend fun revokeToken(call: RoutingCall, request: RefreshToken, userTokenRepository: UserTokenRepository) {
        userTokenRepository.revokeToken(request.refreshToken)
        call.respondText("", status = HttpStatusCode.OK)
    }
}