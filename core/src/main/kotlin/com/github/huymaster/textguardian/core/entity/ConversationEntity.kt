package com.github.huymaster.textguardian.core.entity

import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*

interface ConversationEntity : BaseEntity<ConversationEntity> {
    companion object : Entity.Factory<ConversationEntity>()

    var conversationId: UUID
    var createdAt: Instant
    var name: String
    var creator: UUID
}