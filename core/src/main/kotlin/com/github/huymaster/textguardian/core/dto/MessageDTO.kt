package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.entity.MessageEntity
import java.time.Instant
import java.util.*

class MessageDTO : BaseDTOImpl<MessageEntity>() {
    companion object {
        private val encoder = Base64.getEncoder()
        private val decoder = Base64.getDecoder()
        const val ID_FIELD = "messageId"
        const val CONVERSATION_ID_FIELD = "conversationId"
        const val SENDER_ID_FIELD = "senderId"
        const val SEND_AT_FIELD = "sendAt"
        const val SESSION_KEYS_FIELD = "sessionKeys"
        const val REPLY_TO_FIELD = "replyTo"
    }

    lateinit var messageId: UUID
    lateinit var conversationId: UUID
    lateinit var senderId: UUID
    lateinit var sendAt: Instant
    var sessionKeys: List<String> = emptyList()
    var replyTo: UUID? = null

    override fun write(output: ObjectNode) {
        output.put(ID_FIELD, messageId.toString())
        output.put(CONVERSATION_ID_FIELD, conversationId.toString())
        output.put(SENDER_ID_FIELD, senderId.toString())
        output.put(SEND_AT_FIELD, sendAt.toEpochMilli())
        val array = output.putArray(SESSION_KEYS_FIELD)
        sessionKeys.forEach(array::add)
        output.put(REPLY_TO_FIELD, replyTo?.toString())
    }

    override fun read(input: JsonNode) {
        messageId = UUID.fromString(input.getOrThrow(ID_FIELD).asText())
        conversationId = UUID.fromString(input.getOrThrow(CONVERSATION_ID_FIELD).asText())
        senderId = UUID.fromString(input.getOrThrow(SENDER_ID_FIELD).asText())
        sendAt = Instant.ofEpochMilli(input.getOrDefault(SEND_AT_FIELD, 0L).asLong())
        replyTo = input.getOrNull(REPLY_TO_FIELD)?.let { UUID.fromString(it.asText()) }
        val array = input.getOrDefault(SESSION_KEYS_FIELD, ArrayNode(JsonNodeFactory.instance))
        if (array.isArray && array is ArrayNode)
            sessionKeys = array.map(JsonNode::asText)
    }

    override fun toEntity(): MessageEntity {
        return MessageEntity().apply {
            messageId = this@MessageDTO.messageId
            conversationId = this@MessageDTO.conversationId
            senderId = this@MessageDTO.senderId
            sendAt = this@MessageDTO.sendAt
            sessionKeys = this@MessageDTO.sessionKeys.map { decoder.decode(it) }.toTypedArray()
            replyTo = this@MessageDTO.replyTo
        }
    }

    override fun toDTO(entity: MessageEntity): BaseDTO<MessageEntity> {
        return MessageDTO().apply {
            messageId = entity.messageId
            conversationId = entity.conversationId
            senderId = entity.senderId
            sendAt = entity.sendAt
            sessionKeys = entity.sessionKeys.map { encoder.encodeToString(it) }
            replyTo = entity.replyTo
        }
    }

    override fun exportTo(entity: MessageEntity) {
        entity.messageId = messageId
        entity.conversationId = conversationId
        entity.senderId = senderId
        entity.sendAt = sendAt
        entity.sessionKeys = sessionKeys.map { decoder.decode(it) }.toTypedArray()
        entity.replyTo = replyTo
    }

    override fun importFrom(entity: MessageEntity) {
        messageId = entity.messageId
        conversationId = entity.conversationId
        senderId = entity.senderId
        sendAt = entity.sendAt
        sessionKeys = entity.sessionKeys.map { encoder.encodeToString(it) }
        replyTo = entity.replyTo
    }
}