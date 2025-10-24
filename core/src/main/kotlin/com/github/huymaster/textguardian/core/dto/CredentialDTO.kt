package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.entity.CredentialEntity
import org.ktorm.entity.Entity
import java.util.*

class CredentialDTO(
    entity: CredentialEntity? = null
) : BaseDTOImpl<CredentialEntity>(entity) {
    companion object {
        private val encoder = Base64.getEncoder()
        private val decoder = Base64.getDecoder()

        const val ID_FIELD = "userId"
        const val PASSWORD_FIELD = "password"
    }

    lateinit var userId: UUID
    lateinit var password: ByteArray

    override fun write(output: ObjectNode) {
        output.put(ID_FIELD, userId.toString())
        output.put(PASSWORD_FIELD, encoder.encodeToString(password))
    }

    override fun read(input: JsonNode) {
        userId = UUID.fromString(input.getOrThrow(ID_FIELD).asText())
        password = decoder.decode(input.getOrThrow(PASSWORD_FIELD).asText())
    }

    override fun toEntity(): CredentialEntity {
        return Entity.create<CredentialEntity>().apply {
            this.userId = this@CredentialDTO.userId
            this.password = this@CredentialDTO.password
        }
    }

    override fun toDTO(entity: CredentialEntity): BaseDTO<CredentialEntity> {
        return CredentialDTO().apply {
            this.userId = entity.userId
            this.password = entity.password
        }
    }

    override fun exportTo(entity: CredentialEntity) {
        entity.userId = userId
        entity.password = password
    }

    override fun importFrom(entity: CredentialEntity) {
        userId = entity.userId
        password = entity.password
    }
}