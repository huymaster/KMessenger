package com.github.huymaster.textguardian.core.entity

import org.ktorm.entity.Entity
import java.util.*

interface MessageAttachmentEntity : BaseEntity<MessageAttachmentEntity> {
    companion object : Entity.Factory<MessageAttachmentEntity>()

    var messageId: UUID
    var attachmentId: UUID
}