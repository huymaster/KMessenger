package com.github.huymaster.textguardian.core.entity

import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*

interface MessageEntity : BaseEntity<MessageEntity> {
    companion object : Entity.Factory<MessageEntity>()

    var messageId: UUID
    var conversationId: UUID
    var senderId: UUID
    var sendAt: Instant
    var sessionKeys: Array<ByteArray>
    var replyTo: UUID?
}