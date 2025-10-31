package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.core.api.type.BasicUserInfo
import com.github.huymaster.textguardian.core.api.type.UserInfo
import com.github.huymaster.textguardian.core.dto.UserDTO
import com.github.huymaster.textguardian.core.entity.UserEntity
import com.github.huymaster.textguardian.server.api.APIVersion1.receiveNullableNoThrow
import com.github.huymaster.textguardian.server.api.APIVersion1.sendErrorResponse
import com.github.huymaster.textguardian.server.api.SubRoute
import com.github.huymaster.textguardian.server.data.repository.RepositoryResult
import com.github.huymaster.textguardian.server.data.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.core.component.inject
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import java.util.*

object UserRoute : SubRoute() {
    suspend fun getMe(call: ApplicationCall) {
        val user: UserRepository by inject()
        val principal = call.principal<JWTPrincipal>()
        val payload = principal?.payload?.getClaim(UserDTO.ID_FIELD)?.asString()
        val rr = user.getUserByUserId(payload)
        if ((payload == null) || (rr is RepositoryResult.Error) || ((rr as RepositoryResult.Success<*>).data !is UserEntity)) {
            call.sendErrorResponse("Not allowed to retrieve user info", status = HttpStatusCode.Unauthorized)
            return
        }
        call.respond(UserInfo(rr.data as UserEntity))
    }

    suspend fun updateInfo(call: ApplicationCall) {
        val user: UserRepository by inject()
        val principal = call.principal<JWTPrincipal>()
        val payload = principal?.payload?.getClaim(UserDTO.ID_FIELD)?.asString()
        val userId = runCatching { UUID.fromString(payload) }.getOrNull()
        if (userId == null) {
            call.sendErrorResponse("Invalid user id")
            return
        }
        val request = call.receiveNullableNoThrow<BasicUserInfo>()
        if (request == null) {
            call.sendErrorResponse("Invalid request.")
            return
        }
        val updated = mutableListOf<String>()
        val error = mutableMapOf<String, RepositoryResult.Error>()
        user.updateUsername(userId, request.username)
            .ifStatus(HttpStatusCode.OK) { updated.add(UserDTO.USERNAME_FIELD) }
            .ifError { error[UserDTO.USERNAME_FIELD] = it }
        user.updateDisplayName(userId, request.displayName)
            .ifStatus(HttpStatusCode.OK) { updated.add(UserDTO.DISPLAY_NAME_FIELD) }
            .ifError { error[UserDTO.DISPLAY_NAME_FIELD] = it }
        user.updateLastSeen(userId, request.lastSeen)
            .ifStatus(HttpStatusCode.OK) { updated.add(UserDTO.LAST_SEEN_FIELD) }
            .ifError { error[UserDTO.LAST_SEEN_FIELD] = it }
        user.updateAvatar(userId, request.avatarUrl)
            .ifStatus(HttpStatusCode.OK) { updated.add(UserDTO.AVATAR_URL_FIELD) }
            .ifError { error[UserDTO.AVATAR_URL_FIELD] = it }
        user.updateBio(userId, request.bio)
            .ifStatus(HttpStatusCode.OK) { updated.add(UserDTO.BIO_FIELD) }
            .ifError { error[UserDTO.BIO_FIELD] = it }

        if (error.isNotEmpty())
            call.sendErrorResponse(error.values.first())
        else if (updated.isEmpty())
            call.respond(HttpStatusCode.NoContent, emptyList<String>())
        else
            call.respond(updated)
    }

    suspend fun getUsers(call: ApplicationCall) {
        if (call.parameters.contains(UserDTO.USERNAME_FIELD))
            getUserByUsername(call, call.parameters[UserDTO.USERNAME_FIELD]!!)
        else if (call.parameters.contains(UserDTO.PHONE_NUMBER_FIELD))
            getUserByPhoneNumber(call, call.parameters[UserDTO.PHONE_NUMBER_FIELD]!!)
        else
            call.sendErrorResponse("Invalid request. Must have one of [${UserDTO.USERNAME_FIELD}, ${UserDTO.PHONE_NUMBER_FIELD}] parameters")
    }

    private suspend fun getUserByUsername(call: ApplicationCall, username: String) {
        val user: UserRepository by inject()
        if (username.isBlank()) {
            call.sendErrorResponse("Invalid username")
            return
        }
        val e = user.findAll { it.username like "${username}%" }
        if (e.isEmpty()) {
            call.sendErrorResponse("User not found", status = HttpStatusCode.NotFound)
            return
        }
        call.respond(e.map { BasicUserInfo(it) })
    }

    private suspend fun getUserByPhoneNumber(call: ApplicationCall, phoneNumber: String) {
        val user: UserRepository by inject()
        if (phoneNumber.isBlank()) {
            call.sendErrorResponse("Invalid phone number")
            return
        }
        val e = user.findAll { it.phoneNumber eq phoneNumber }
        if (e.isEmpty()) {
            call.sendErrorResponse("User not found", status = HttpStatusCode.NotFound)
            return
        }
        call.respond(e.map { BasicUserInfo(it) })
    }
}