package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.core.api.type.Message
import com.github.huymaster.textguardian.server.api.APIVersion1.getClaim
import com.github.huymaster.textguardian.server.api.APIVersion1.receiveNullableNoThrow
import com.github.huymaster.textguardian.server.api.APIVersion1.sendErrorResponse
import com.github.huymaster.textguardian.server.api.SubRoute
import com.github.huymaster.textguardian.server.data.repository.MessageRepository
import com.github.huymaster.textguardian.server.data.repository.RepositoryResult
import com.github.huymaster.textguardian.server.net.USER_ID_CLAIM
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.koin.core.component.inject
import java.util.*

object ChatRoute : SubRoute() {
    suspend fun sendMessage(call: ApplicationCall) {
        val userId = call.getClaim(USER_ID_CLAIM) { UUID.fromString(asString()) }
        val conversationId = runCatching { UUID.fromString(call.parameters["conversationId"]) }.getOrNull()
        val message = call.receiveNullableNoThrow<Message>()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return
        }
        if (conversationId == null || message == null) {
            call.respond(HttpStatusCode.BadRequest)
            return
        }

        val repository: MessageRepository by inject()
        val result = repository.addMessage(userId, conversationId, message)
        if (result is RepositoryResult.Error) {
            call.sendErrorResponse(result)
            return
        }
        call.respond(HttpStatusCode.OK)
    }

    suspend fun getMessage(call: ApplicationCall) {
        val userId = call.getClaim(USER_ID_CLAIM) { UUID.fromString(asString()) }
        val conversationId = runCatching { UUID.fromString(call.parameters["conversationId"]) }.getOrNull()
        val messageId = runCatching { UUID.fromString(call.parameters["messageId"]) }.getOrNull()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return
        }
        if (conversationId == null || messageId == null) {
            call.respond(HttpStatusCode.BadRequest)
            return
        }

        val repository: MessageRepository by inject()
        val result = repository.getMessage(userId, conversationId, messageId)
        if (result !is RepositoryResult.Success<*>) {
            call.sendErrorResponse(result)
            return
        }
        call.respond(result.data as Message)
    }

    suspend fun getMessages(call: ApplicationCall) {
        val userId = call.getClaim(USER_ID_CLAIM) { UUID.fromString(asString()) }
        val conversationId = runCatching { UUID.fromString(call.parameters["conversationId"]) }.getOrNull()
        val startMessageId = runCatching { UUID.fromString(call.request.queryParameters["from"]) }.getOrNull()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return
        }
        if (conversationId == null) {
            call.respond(HttpStatusCode.BadRequest)
            return
        }

        val repository: MessageRepository by inject()
        val result = repository.getMessages(userId, conversationId, startMessageId)
        if (result !is RepositoryResult.Success<*>) {
            call.sendErrorResponse(result)
            return
        }
        call.respond(result.data as List<*>)
    }

    suspend fun getLatestMessages(call: ApplicationCall) {
        val userId = call.getClaim(USER_ID_CLAIM) { UUID.fromString(asString()) }
        val conversationId = runCatching { UUID.fromString(call.parameters["conversationId"]) }.getOrNull()
        val since = runCatching { UUID.fromString(call.request.queryParameters["since"]) }.getOrNull()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return
        }
        if (conversationId == null) {
            call.respond(HttpStatusCode.BadRequest)
            return
        }
        if (since == null) {
            getMessages(call)
            return
        }

        val repository: MessageRepository by inject()
        val result = repository.getLastestMessages(userId, conversationId, since)
        if (result !is RepositoryResult.Success<*>) {
            call.sendErrorResponse(result)
            return
        }
        call.respond(result.data as List<*>)
    }
}