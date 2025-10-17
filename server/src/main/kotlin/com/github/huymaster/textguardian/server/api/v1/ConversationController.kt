package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.core.dto.ConversationDTO
import com.github.huymaster.textguardian.core.dto.UserDTO
import com.github.huymaster.textguardian.server.api.APIVersion1.sendErrorResponse
import com.github.huymaster.textguardian.server.data.repository.ConversationRepository
import com.github.huymaster.textguardian.server.data.repository.MessageRepository
import com.github.huymaster.textguardian.server.data.repository.ParticipantRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

object ConversationController {
    suspend fun getConversationList(
        call: RoutingCall,
        conversationRepository: ConversationRepository,
        participantRepository: ParticipantRepository
    ) {
        val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(UserDTO.ID_FIELD)?.asString()
        val userUuid = runCatching { UUID.fromString(userId) }
        if (userUuid.isFailure || userUuid.getOrNull() == null) {
            sendErrorResponse(call, "Can't get user", userUuid.exceptionOrNull(), HttpStatusCode.Unauthorized)
            return
        }
        val uuid = userUuid.getOrNull()!!
        val conversationList = conversationRepository.getAllConversation(uuid, participantRepository)
            .map { ConversationDTO().apply { importFrom(it) } }
        call.respond(conversationList)
    }

    suspend fun getConversationById(
        call: RoutingCall,
        conversationRepository: ConversationRepository
    ) {
        val conversationId = runCatching { UUID.fromString(call.parameters[ConversationDTO.ID_FIELD]) }
        if (conversationId.isFailure || conversationId.getOrNull() == null) {
            sendErrorResponse(
                call, "Require conversationId parameter", conversationId.exceptionOrNull(),
                HttpStatusCode.BadRequest
            )
            return
        }
        val conversation = conversationRepository.findConversationByConversationId(conversationId.getOrNull()!!)
        if (conversation == null) {
            sendErrorResponse(call, "Conversation not found", status = HttpStatusCode.NotFound)
            return
        }
        val conversationDTO = ConversationDTO().apply { importFrom(conversation) }
        call.respond(conversationDTO)
    }

    suspend fun getConversationName(
        call: RoutingCall,
        conversationRepository: ConversationRepository
    ) {
        val conversationId = runCatching { UUID.fromString(call.parameters[ConversationDTO.ID_FIELD]) }
        if (conversationId.isFailure || conversationId.getOrNull() == null) {
            sendErrorResponse(
                call, "Require ${ConversationDTO.ID_FIELD} parameter", conversationId.exceptionOrNull(),
                HttpStatusCode.BadRequest
            )
            return
        }
        val conversation = conversationRepository.findConversationByConversationId(conversationId.getOrNull()!!)
        if (conversation == null) {
            sendErrorResponse(call, "Conversation not found", status = HttpStatusCode.NotFound)
            return
        }
        call.respond(conversation.name)
    }

    suspend fun getLatestMessage(
        call: RoutingCall,
        conversationRepository: ConversationRepository,
        messageRepository: MessageRepository
    ) {
        val conversationId = runCatching { UUID.fromString(call.parameters[ConversationDTO.ID_FIELD]) }
        if (conversationId.isFailure || conversationId.getOrNull() == null) {
            sendErrorResponse(
                call, "Require ${ConversationDTO.ID_FIELD} parameter", conversationId.exceptionOrNull(),
                HttpStatusCode.BadRequest
            )
            return
        }
        val conversation = conversationRepository.findConversationByConversationId(conversationId.getOrNull()!!)
        if (conversation == null) {
            sendErrorResponse(call, "Conversation not found", status = HttpStatusCode.NotFound)
            return
        }
        // TODO: get latest message
    }
}