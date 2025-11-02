package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.core.api.type.BasicUserInfo
import com.github.huymaster.textguardian.core.api.type.UserInfo
import com.github.huymaster.textguardian.core.entity.UserEntity
import com.github.huymaster.textguardian.server.api.APIVersion1.getClaim
import com.github.huymaster.textguardian.server.api.APIVersion1.receiveNullableNoThrow
import com.github.huymaster.textguardian.server.api.APIVersion1.sendErrorResponse
import com.github.huymaster.textguardian.server.api.SubRoute
import com.github.huymaster.textguardian.server.data.repository.RepositoryResult
import com.github.huymaster.textguardian.server.data.repository.UserRepository
import com.github.huymaster.textguardian.server.net.USER_ID_CLAIM
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.koin.core.component.inject
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import java.util.*

object UserRoute : SubRoute() {
    suspend fun getMe(call: ApplicationCall) {
        val user: UserRepository by inject()
        val payload = call.getClaim(USER_ID_CLAIM) { asString() }
        val rr = user.getUserByUserId(payload)
        if ((payload == null) || (rr is RepositoryResult.Error) || ((rr as RepositoryResult.Success<*>).data !is UserEntity)) {
            call.sendErrorResponse("Not allowed to retrieve user info", status = HttpStatusCode.Unauthorized)
            return
        }
        call.respond(UserInfo(rr.data as UserEntity))
    }

    suspend fun updateInfo(call: ApplicationCall) {
        val user: UserRepository by inject()
        val payload = call.getClaim(USER_ID_CLAIM) { asString() }
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
            .ifStatus(HttpStatusCode.OK) { updated.add(USER_ID_CLAIM) }
            .ifError { error[USER_ID_CLAIM] = it }
        user.updateDisplayName(userId, request.displayName)
            .ifStatus(HttpStatusCode.OK) { updated.add("displayName") }
            .ifError { error["displayName"] = it }
        user.updateLastSeen(userId, request.lastSeen)
            .ifStatus(HttpStatusCode.OK) { updated.add("lastSeen") }
            .ifError { error["lastSeen"] = it }
        user.updateAvatar(userId, request.avatarUrl)
            .ifStatus(HttpStatusCode.OK) { updated.add("avatarUrl") }
            .ifError { error["avatarUrl"] = it }
        user.updateBio(userId, request.bio)
            .ifStatus(HttpStatusCode.OK) { updated.add("bio") }
            .ifError { error["bio"] = it }

        if (error.isNotEmpty())
            call.sendErrorResponse(error.values.first())
        else if (updated.isEmpty())
            call.respond(HttpStatusCode.NoContent, emptyList<String>())
        else
            call.respond(updated)
    }

    suspend fun getUsers(call: ApplicationCall) {
        if (call.parameters.contains("username"))
            getUserByUsername(call, call.parameters["username"]!!)
        else if (call.parameters.contains("phoneNumber"))
            getUserByPhoneNumber(call, call.parameters["phoneNumber"]!!)
        else
            call.sendErrorResponse("Invalid request. Must have one of [username, phoneNumber] parameters")
    }

    private suspend fun getUserByUsername(call: ApplicationCall, username: String) {
        val user: UserRepository by inject()
        val normalizeUsername = username.trimIndent().lowercase()
        if (normalizeUsername.isBlank()) {
            call.sendErrorResponse("Invalid username")
            return
        }
        val e = user.findAll { it.username like "${normalizeUsername}%" }
        if (e.isEmpty()) {
            call.sendErrorResponse("No user found", status = HttpStatusCode.NotFound)
            return
        }
        call.respond(e.map { BasicUserInfo(it, true) })
    }

    private suspend fun getUserByPhoneNumber(call: ApplicationCall, phoneNumber: String) {
        val user: UserRepository by inject()
        if (phoneNumber.isBlank()) {
            call.sendErrorResponse("Invalid phone number")
            return
        }
        val e = user.findAll { it.phoneNumber eq phoneNumber }
        if (e.isEmpty()) {
            call.sendErrorResponse("No user found", status = HttpStatusCode.NotFound)
            return
        }
        call.respond(e.map { BasicUserInfo(it) })
    }
}