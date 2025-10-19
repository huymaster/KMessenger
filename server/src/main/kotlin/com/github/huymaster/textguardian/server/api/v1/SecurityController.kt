package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.core.api.type.UserPublicKey
import com.github.huymaster.textguardian.core.dto.ConversationDTO
import com.github.huymaster.textguardian.server.api.APIVersion1.sendErrorResponse
import com.github.huymaster.textguardian.server.data.repository.ConversationRepository
import com.github.huymaster.textguardian.server.data.repository.ParticipantRepository
import com.github.huymaster.textguardian.server.data.repository.PublicKeyRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.eq
import java.util.*

object SecurityController {
    suspend fun getParticipantPublicKeys(
        call: RoutingCall,
        conversationRepository: ConversationRepository,
        participantRepository: ParticipantRepository,
        publicKeyRepository: PublicKeyRepository
    ) {
        val conversationId = runCatching { UUID.fromString(call.parameters[ConversationDTO.ID_FIELD]) }
        if (conversationId.isFailure || conversationId.getOrNull() == null) {
            sendErrorResponse(
                call, "Require conversationId parameter", conversationId.exceptionOrNull(),
                HttpStatusCode.BadRequest
            )
            return
        }
        val conversation = conversationId.getOrNull()!!
        if (!conversationRepository.exists { it.conversationId eq conversation }) {
            sendErrorResponse(call, "Conversation not found", status = HttpStatusCode.NotFound)
            return
        }
        val participants = participantRepository.getParticipantsByConversationId(conversation)
        val keys = mutableListOf<UserPublicKey>()
        participants.forEach { participant ->
            val array = publicKeyRepository.getPublicKeyByUserId(participant.userId) ?: return@forEach
            array.forEach { key -> keys.add(UserPublicKey(participant.userId, key)) }
        }
        call.respond(keys)
    }
}