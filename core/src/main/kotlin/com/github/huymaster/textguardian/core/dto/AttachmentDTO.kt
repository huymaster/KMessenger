package com.github.huymaster.textguardian.core.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.huymaster.textguardian.core.entity.AttachmentEntity
import java.util.*

class AttachmentDTO : BaseDTOImpl<AttachmentEntity>() {
    companion object {
        const val ID_FIELD = "attachmentId"
        const val MIMETYPE_FIELD = "mimeType"
        const val FILE_SIZE_FIELD = "fileSize"
    }

    lateinit var attachmentId: UUID
    var mimeType: String = "application/octet-stream"
    var fileSize: Long = 0
    override fun write(output: ObjectNode) {
        output.put(ID_FIELD, attachmentId.toString())
        output.put(MIMETYPE_FIELD, mimeType)
        output.put(FILE_SIZE_FIELD, fileSize)
    }

    override fun read(input: JsonNode) {
        attachmentId = UUID.fromString(input.getOrThrow(ID_FIELD).asText())
        mimeType = input.getOrDefault(MIMETYPE_FIELD, "application/octet-stream").asText()
        fileSize = input.getOrDefault(FILE_SIZE_FIELD, 0L).asLong()
    }

    override fun toEntity(): AttachmentEntity {
        return AttachmentEntity().apply {
            this.attachmentId = this@AttachmentDTO.attachmentId
            this.mimeType = this@AttachmentDTO.mimeType
            this.fileSize = this@AttachmentDTO.fileSize
        }
    }

    override fun toDTO(entity: AttachmentEntity): BaseDTO<AttachmentEntity> {
        return AttachmentDTO().apply {
            this.attachmentId = entity.attachmentId
            this.mimeType = entity.mimeType
            this.fileSize = entity.fileSize
        }
    }

    override fun exportTo(entity: AttachmentEntity) {
        entity.attachmentId = attachmentId
        entity.mimeType = mimeType
        entity.fileSize = fileSize
    }

    override fun importFrom(entity: AttachmentEntity) {
        attachmentId = entity.attachmentId
        mimeType = entity.mimeType
        fileSize = entity.fileSize
    }
}