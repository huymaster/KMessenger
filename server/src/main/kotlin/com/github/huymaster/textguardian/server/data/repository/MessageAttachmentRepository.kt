package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.MessageAttachmentEntity
import com.github.huymaster.textguardian.server.data.table.MessageAttachmentTable

class MessageAttachmentRepository : BaseRepository<MessageAttachmentEntity, MessageAttachmentTable>(
    MessageAttachmentTable
) {
}