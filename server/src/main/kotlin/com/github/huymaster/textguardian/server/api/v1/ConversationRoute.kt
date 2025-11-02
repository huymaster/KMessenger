package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.core.api.type.CreateConversationRequest
import com.github.huymaster.textguardian.server.api.APIVersion1.getClaim
import com.github.huymaster.textguardian.server.api.APIVersion1.receiveNullableNoThrow
import com.github.huymaster.textguardian.server.api.APIVersion1.sendErrorResponse
import com.github.huymaster.textguardian.server.api.SubRoute
import com.github.huymaster.textguardian.server.data.repository.ConversationRepository
import com.github.huymaster.textguardian.server.data.repository.RepositoryResult
import com.github.huymaster.textguardian.server.net.USER_ID_CLAIM
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.inject
import java.util.*

object ConversationRoute : SubRoute() {
    suspend fun getConversations(call: RoutingCall) {
        val conversation: ConversationRepository by inject()
        val userId = call.getClaim(USER_ID_CLAIM) { UUID.fromString(asString()) }
        if (userId == null) {
            call.sendErrorResponse("Not allowed to retrieve conversations", status = HttpStatusCode.Unauthorized)
            return
        }
        val rr = conversation.getConversationsByUser(userId)
        if (rr !is RepositoryResult.Success<*>) {
            call.sendErrorResponse(rr)
            return
        }
        call.respond(rr.data as List<*>)
    }

    suspend fun createConvertation(call: ApplicationCall) {
        val conversation: ConversationRepository by inject()
        val userId = call.getClaim(USER_ID_CLAIM) { UUID.fromString(asString()) }
        if (userId == null) {
            call.sendErrorResponse("Not allowed to retrieve conversations", status = HttpStatusCode.Unauthorized)
            return
        }
        val request = call.receiveNullableNoThrow<CreateConversationRequest>()
        if (request == null) {
            call.sendErrorResponse("Invalid request. Must have [name] field.")
            return
        }
        val rr = conversation.newConversation(userId, request.name)
        if (rr !is RepositoryResult.Success<*>) {
            call.sendErrorResponse(rr)
        } else {
            call.respond(rr.data as UUID)
        }
    }
}