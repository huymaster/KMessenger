package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.entity.MessageAttachmentEntity
import java.util.*

class MessageAttachmentDTO : BaseDTOImpl<MessageAttachmentEntity>() {
    companion object {
        const val MESSAGE_ID_FIELD = "messageId"
        const val ATTACHMENT_ID_FIELD = "attachmentId"
    }

    lateinit var messageId: UUID
    lateinit var attachmentId: UUID
    override fun write(output: ObjectNode) {
        output.put(MESSAGE_ID_FIELD, messageId.toString())
        output.put(ATTACHMENT_ID_FIELD, attachmentId.toString())
    }

    override fun read(input: JsonNode) {
        messageId = UUID.fromString(input.getOrThrow(MESSAGE_ID_FIELD).asText())
        attachmentId = UUID.fromString(input.getOrThrow(ATTACHMENT_ID_FIELD).asText())
    }

    override fun toEntity(): MessageAttachmentEntity {
        return MessageAttachmentEntity().apply {
            messageId = this@MessageAttachmentDTO.messageId
            attachmentId = this@MessageAttachmentDTO.attachmentId
        }
    }

    override fun toDTO(entity: MessageAttachmentEntity): BaseDTO<MessageAttachmentEntity> {
        return MessageAttachmentDTO().apply {
            messageId = entity.messageId
            attachmentId = entity.attachmentId
        }
    }

    override fun exportTo(entity: MessageAttachmentEntity) {
        entity.messageId = messageId
        entity.attachmentId = attachmentId
    }

    override fun importFrom(entity: MessageAttachmentEntity) {
        messageId = entity.messageId
        attachmentId = entity.attachmentId
    }
}