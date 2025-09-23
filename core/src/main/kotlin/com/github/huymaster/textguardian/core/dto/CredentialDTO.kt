package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.entity.CredentialEntity
import org.ktorm.entity.Entity
import java.util.*

class CredentialDTO : BaseDTOImpl<CredentialEntity>() {
    companion object {
        private val encoder = Base64.getEncoder()
        private val decoder = Base64.getDecoder()

        const val ID_FIELD = "userId"
        const val PASSWORD_FIELD = "password"
        const val KEY_FIELD = "key"
    }

    lateinit var id: UUID
    lateinit var password: ByteArray
    lateinit var key: ByteArray

    override fun write(output: ObjectNode) {
        output.put(ID_FIELD, id.toString())
        output.put(PASSWORD_FIELD, encoder.encodeToString(password))
        output.put(KEY_FIELD, encoder.encodeToString(key))
    }

    override fun read(input: JsonNode) {
        id = UUID.fromString(input.getOrThrow(ID_FIELD).asText())
        password = decoder.decode(input.getOrThrow(PASSWORD_FIELD).asText())
        key = decoder.decode(input.getOrThrow(KEY_FIELD).asText())
    }

    override fun toEntity(): CredentialEntity {
        return Entity.create<CredentialEntity>().apply {
            this.id = this@CredentialDTO.id
            this.password = this@CredentialDTO.password
            this.key = this@CredentialDTO.key
        }
    }

    override fun toDTO(entity: CredentialEntity): BaseDTO<CredentialEntity> {
        return CredentialDTO().apply {
            id = entity.id
            password = entity.password
            key = entity.key
        }
    }

    override fun mergeTo(entity: CredentialEntity) {
        entity.id = id
        entity.password = password
        entity.key = key
    }

    override fun importFrom(entity: CredentialEntity) {
        id = entity.id
        password = entity.password
        key = entity.key
    }
}