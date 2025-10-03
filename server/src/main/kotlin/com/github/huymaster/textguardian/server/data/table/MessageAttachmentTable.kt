package com.github.huymaster.textguardian.server.data.table

import com.github.huymaster.textguardian.core.entity.MessageAttachmentEntity
import org.ktorm.schema.uuid

object MessageAttachmentTable : BaseTable<MessageAttachmentEntity>("message_attachment") {
    val messageId = uuid("message_id")
        .bindTo { it.messageId }
    val attachmentId = uuid("attachment_id")
        .bindTo { it.attachmentId }
}