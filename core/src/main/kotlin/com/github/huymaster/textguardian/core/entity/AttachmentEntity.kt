package com.github.huymaster.textguardian.core.entity

import org.ktorm.entity.Entity
import java.util.*

interface AttachmentEntity : BaseEntity<AttachmentEntity> {
    companion object : Entity.Factory<AttachmentEntity>()

    var attachmentId: UUID
    var mimeType: String
    var fileSize: Long
}