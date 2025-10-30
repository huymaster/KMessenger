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
}