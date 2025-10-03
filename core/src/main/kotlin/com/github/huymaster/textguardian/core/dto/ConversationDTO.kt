package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.entity.ConversationEntity
import java.time.Instant
import java.util.*

class ConversationDTO : BaseDTOImpl<ConversationEntity>() {
    companion object {
        const val ID_FIELD = "conversationId"
        const val CREATED_AT_FIELD = "createdAt"
        const val NAME_FIELD = "name"
    }

    lateinit var conversationId: UUID
    lateinit var createdAt: Instant
    var name: String = ""
    override fun write(output: ObjectNode) {
        output.put(ID_FIELD, conversationId.toString())
        output.put(CREATED_AT_FIELD, createdAt.toEpochMilli())
    }

    override fun read(input: JsonNode) {
        conversationId = UUID.fromString(input.getOrThrow(ID_FIELD).asText())
        createdAt = Instant.ofEpochMilli(input.getOrDefault(CREATED_AT_FIELD, 0L).asLong())
    }

    override fun toEntity(): ConversationEntity {
        return ConversationEntity().apply {
            this.conversationId = this@ConversationDTO.conversationId
            this.createdAt = this@ConversationDTO.createdAt
        }
    }

    override fun toDTO(entity: ConversationEntity): BaseDTO<ConversationEntity> {
        return ConversationDTO().apply {
            this.conversationId = entity.conversationId
            this.createdAt = entity.createdAt
        }
    }

    override fun exportTo(entity: ConversationEntity) {
        entity.conversationId = conversationId
        entity.createdAt = createdAt
    }

    override fun importFrom(entity: ConversationEntity) {
        conversationId = entity.conversationId
        createdAt = entity.createdAt
    }
}