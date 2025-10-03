package com.github.huymaster.textguardian.core.entity

import org.ktorm.entity.Entity
import java.util.*

interface ParticipantEntity : BaseEntity<ParticipantEntity> {
    companion object : Entity.Factory<ParticipantEntity>()

    var conversationId: UUID
    var userId: UUID
}