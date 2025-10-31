package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.core.api.type.AccessToken
import com.github.huymaster.textguardian.core.api.type.LoginRequest
import com.github.huymaster.textguardian.core.api.type.RefreshToken
import com.github.huymaster.textguardian.core.api.type.RegisterRequest
import com.github.huymaster.textguardian.core.dto.UserDTO
import com.github.huymaster.textguardian.core.entity.UserEntity
import com.github.huymaster.textguardian.server.api.APIVersion1.receiveNullableNoThrow
import com.github.huymaster.textguardian.server.api.APIVersion1.sendErrorResponse
import com.github.huymaster.textguardian.server.api.SubRoute
import com.github.huymaster.textguardian.server.data.repository.CredentialRepository
import com.github.huymaster.textguardian.server.data.repository.RepositoryResult
import com.github.huymaster.textguardian.server.data.repository.UserRepository
import com.github.huymaster.textguardian.server.data.repository.UserTokenRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.core.component.inject
import org.ktorm.database.Database
import java.util.*

object AuthRoute : SubRoute() {
    suspend fun register(call: ApplicationCall) {
        val database: Database by inject()
        val user: UserRepository by inject()
        val credential: CredentialRepository by inject()

        val request = call.receiveNullableNoThrow<RegisterRequest>()
        if (request == null) {
            call.sendErrorResponse("Invalid request. [phoneNumber, password] required")
            return
        }

        database.useTransaction { transaction ->
            val newUserResult = user.newUser(request.phoneNumber)
            if (newUserResult !is RepositoryResult.Success<*>) {
                call.sendErrorResponse(newUserResult)
                transaction.rollback()
                return
            }
            val newUser = newUserResult.data as UserEntity
            val newCredentialResult = credential.newCredential(newUser.userId, request.password)
            if (newCredentialResult !is RepositoryResult.Success<*>) {
                call.sendErrorResponse(newCredentialResult)
                transaction.rollback()
                return
            }

            transaction.commit()
            call.respond(message = "OK", status = HttpStatusCode.OK)
        }
    }

    suspend fun login(call: ApplicationCall) {
        val user: UserRepository by inject()
        val credential: CredentialRepository by inject()
        val token: UserTokenRepository by inject()

        val request = call.receiveNullableNoThrow<LoginRequest>()
        if (request == null) {
            call.sendErrorResponse("Invalid request. [phoneNumber, password] required")
            return
        }

        val findUserResult = user.getUserByPhoneNumber(phoneNumber = request.phoneNumber)
        if (findUserResult !is RepositoryResult.Success<*>) {
            call.sendErrorResponse(findUserResult)
            return
        }
        val localUser = findUserResult.data as UserEntity
        val verifyCredentialResult = credential.verifyCredential(localUser.userId, request.password)
        if (verifyCredentialResult !is RepositoryResult.Success<*>) {
            call.sendErrorResponse(verifyCredentialResult)
            return
        }
        val genTokenResult = token.createToken(localUser.userId, request.deviceInfo)
        if (genTokenResult !is RepositoryResult.Success<*>) {
            call.sendErrorResponse(genTokenResult)
            return
        }
        call.respond(genTokenResult.data as RefreshToken)
    }

    suspend fun refresh(call: ApplicationCall) {
        val token: UserTokenRepository by inject()
        val request = call.parameters["refreshToken"]?.decodeURLPart()
        if (request == null || request.isBlank()) {
            call.sendErrorResponse("Invalid request. [refreshToken] required")
            return
        }

        val genTokenResult = token.generateAccessToken(request)
        if (genTokenResult !is RepositoryResult.Success<*>) {
            call.sendErrorResponse(genTokenResult)
            return
        }
        call.respond(genTokenResult.data as AccessToken)
    }

    suspend fun logout(call: ApplicationCall) {
        val token: UserTokenRepository by inject()
        val principal = call.principal<JWTPrincipal>()
        val payload = principal?.payload?.getClaim(UserDTO.ID_FIELD)?.asString()
        if (principal == null || payload == null) {
            call.sendErrorResponse("Not allowed to revoke this token", status = HttpStatusCode.Unauthorized)
            return
        }
        val request = call.parameters["refreshToken"]?.decodeURLPart()
        if (request == null) {
            call.sendErrorResponse("Invalid request. [refreshToken] required")
            return
        }

        val revokeResult = token.revokeToken(request, UUID.fromString(payload))
        if (revokeResult !is RepositoryResult.Success<*>) {
            call.sendErrorResponse(revokeResult)
            return
        }
        call.respondText("OK")
    }
}