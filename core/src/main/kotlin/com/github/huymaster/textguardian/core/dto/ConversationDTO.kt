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
        const val CREATOR = "creator"
    }

    lateinit var conversationId: UUID
    lateinit var createdAt: Instant
    lateinit var name: String
    lateinit var creator: UUID

    override fun write(output: ObjectNode) {
        output.put(ID_FIELD, conversationId.toString())
        output.put(CREATED_AT_FIELD, createdAt.toEpochMilli())
        output.put(NAME_FIELD, name)
        output.put(CREATOR, creator.toString())
    }

    override fun read(input: JsonNode) {
        conversationId = UUID.fromString(input.getOrThrow(ID_FIELD).asText())
        createdAt = Instant.ofEpochMilli(input.getOrDefault(CREATED_AT_FIELD, 0L).asLong())
        name = input.getOrDefault(NAME_FIELD, "").asText()
        creator = UUID.fromString(input.getOrThrow(CREATOR).asText())
    }

    override fun toEntity(): ConversationEntity {
        return ConversationEntity().apply {
            this.conversationId = this@ConversationDTO.conversationId
            this.createdAt = this@ConversationDTO.createdAt
            this.name = this@ConversationDTO.name
            this.creator = this@ConversationDTO.creator
        }
    }

    override fun toDTO(entity: ConversationEntity): BaseDTO<ConversationEntity> {
        return ConversationDTO().apply {
            this.conversationId = entity.conversationId
            this.createdAt = entity.createdAt
            this.name = entity.name
            this.creator = entity.creator
        }
    }

    override fun exportTo(entity: ConversationEntity) {
        entity.conversationId = conversationId
        entity.createdAt = createdAt
        entity.name = name
        entity.creator = creator
    }

    override fun importFrom(entity: ConversationEntity) {
        conversationId = entity.conversationId
        createdAt = entity.createdAt
        name = entity.name
        creator = entity.creator
    }
}