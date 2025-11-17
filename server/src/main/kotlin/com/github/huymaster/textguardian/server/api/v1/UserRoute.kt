package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.core.api.type.UserInfo
import com.github.huymaster.textguardian.core.api.type.UserPublicKey
import com.github.huymaster.textguardian.core.api.type.UserPublicKeys
import com.github.huymaster.textguardian.core.entity.UserEntity
import com.github.huymaster.textguardian.server.api.APIVersion1.getClaim
import com.github.huymaster.textguardian.server.api.APIVersion1.receiveNullableNoThrow
import com.github.huymaster.textguardian.server.api.APIVersion1.sendErrorResponse
import com.github.huymaster.textguardian.server.api.SubRoute
import com.github.huymaster.textguardian.server.data.repository.PublicKeyRepository
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
    suspend fun getUser(call: ApplicationCall) {
        val user: UserRepository by inject()
        val payload = call.getClaim(USER_ID_CLAIM) { asString() }
        val userId = runCatching { call.parameters["userId"].also { UUID.fromString(it) } }
            .onFailure { e ->
                if (payload == null) call.sendErrorResponse("Invalid user id")
            }
            .getOrNull()
        val rr = user.getUserByUserId(userId ?: payload)
        if ((rr is RepositoryResult.Error) || ((rr as RepositoryResult.Success<*>).data !is UserEntity)) {
            call.sendErrorResponse(rr)
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
        val request = call.receiveNullableNoThrow<UserInfo>()
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
        val username = getUserByUsername(call.parameters["username"])
        val phone = getUserByPhoneNumber(call.parameters["phoneNumber"])
        val list = (username + phone).toSet().toList()
        if (list.isEmpty())
            call.respond(HttpStatusCode.NotFound)
        else
            call.respond(list)
    }

    private suspend fun getUserByUsername(username: String?): List<UserInfo> {
        val user: UserRepository by inject()
        username ?: return emptyList()
        val normalizeUsername = username.trimIndent().lowercase()
        if (normalizeUsername.isBlank())
            return emptyList()
        val e = user.findAll { it.username like "${normalizeUsername}%" }
        return e.map { UserInfo(it) }.take(20)
    }

    private suspend fun getUserByPhoneNumber(phoneNumber: String?): List<UserInfo> {
        val user: UserRepository by inject()
        phoneNumber ?: return emptyList()
        if (phoneNumber.isBlank()) return emptyList()

        val e = user.findAll { it.phoneNumber eq phoneNumber }
        if (e.isEmpty()) return emptyList()

        return e.map { UserInfo(it) }.take(20)
    }

    suspend fun getPublicKeys(call: ApplicationCall) {
        val publicKey: PublicKeyRepository by inject()
        val userId = runCatching { call.parameters["userId"] }.getOrNull()
        if (userId == null) {
            call.sendErrorResponse("Invalid user id")
            return
        }
        val rr = publicKey.getPublicKeys(UUID.fromString(userId))
        if ((rr is RepositoryResult.Error) || ((rr as RepositoryResult.Success<*>).data !is UserPublicKeys)) {
            call.sendErrorResponse(rr)
            return
        }
        call.respond(rr.data as UserPublicKeys)
    }

    suspend fun addPublicKey(call: ApplicationCall) {
        val publicKey: PublicKeyRepository by inject()
        val userId = runCatching { call.getClaim(USER_ID_CLAIM) { UUID.fromString(asString()) } }.getOrNull()
        if (userId == null) {
            call.sendErrorResponse("Invalid user id or not authenticated", status = HttpStatusCode.Unauthorized)
            return
        }
        val request = call.receiveNullableNoThrow<UserPublicKey>()
        if (request == null) {
            call.sendErrorResponse("Invalid request. Field [key] is required. Key is Base64 encoded and valid public key.")
            return
        }
        val rr = publicKey.addPublicKey(userId, request.key)
        if ((rr is RepositoryResult.Error)) {
            call.sendErrorResponse(rr)
            return
        }
        call.respond(HttpStatusCode.OK)
    }
}