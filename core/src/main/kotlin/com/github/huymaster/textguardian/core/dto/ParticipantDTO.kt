package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.entity.ParticipantEntity
import java.util.*

class ParticipantDTO : BaseDTOImpl<ParticipantEntity>() {
    companion object {
        const val CONVERSATION_ID_FIELD = "conversationId"
        const val USER_ID_FIELD = "userId"
    }

    lateinit var conversationId: UUID
    lateinit var userId: UUID
    override fun write(output: ObjectNode) {
        output.put(CONVERSATION_ID_FIELD, conversationId.toString())
        output.put(USER_ID_FIELD, userId.toString())
    }

    override fun read(input: JsonNode) {
        conversationId = UUID.fromString(input.getOrThrow(CONVERSATION_ID_FIELD).asText())
        userId = UUID.fromString(input.getOrThrow(USER_ID_FIELD).asText())
    }

    override fun toEntity(): ParticipantEntity {
        return ParticipantEntity().apply {
            this.conversationId = this@ParticipantDTO.conversationId
            this.userId = this@ParticipantDTO.userId
        }
    }

    override fun toDTO(entity: ParticipantEntity): BaseDTO<ParticipantEntity> {
        return ParticipantDTO().apply {
            this.conversationId = entity.conversationId
            this.userId = entity.userId
        }
    }

    override fun exportTo(entity: ParticipantEntity) {
        entity.conversationId = conversationId
        entity.userId = userId
    }

    override fun importFrom(entity: ParticipantEntity) {
        conversationId = entity.conversationId
        userId = entity.userId
    }
}