package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.entity.PublicKeyEntity
import java.util.*

class PublicKeyDTO : BaseDTOImpl<PublicKeyEntity>() {
    companion object {
        private val encoder = Base64.getEncoder()
        private val decoder = Base64.getDecoder()

        private const val USER_ID_FIELD = "userId"
        private const val KEYS_FIELD = "keys"
    }

    lateinit var userId: UUID
    var keys: List<String> = emptyList()

    override fun write(output: ObjectNode) {
        output.put(USER_ID_FIELD, userId.toString())
        val array = output.putArray(KEYS_FIELD)
        keys.forEach(array::add)
    }

    override fun read(input: JsonNode) {
        userId = UUID.fromString(input.getOrThrow(USER_ID_FIELD).asText())
        val array = input.getOrDefault(KEYS_FIELD, ArrayNode(JsonNodeFactory.instance))
        if (array.isArray && array is ArrayNode)
            keys = array.map(JsonNode::asText)
    }

    override fun toEntity(): PublicKeyEntity {
        return PublicKeyEntity().apply {
            userId = this@PublicKeyDTO.userId
            keys = this@PublicKeyDTO.keys.map { decoder.decode(it) }.toTypedArray()
        }
    }

    override fun toDTO(entity: PublicKeyEntity): BaseDTO<PublicKeyEntity> {
        return PublicKeyDTO().apply {
            userId = entity.userId
            keys = entity.keys.map { encoder.encodeToString(it) }.toList()
        }
    }

    override fun exportTo(entity: PublicKeyEntity) {
        entity.userId = userId
        entity.keys = keys.map { decoder.decode(it) }.toTypedArray()
    }

    override fun importFrom(entity: PublicKeyEntity) {
        userId = entity.userId
        keys = entity.keys.map { encoder.encodeToString(it) }
    }
}