package com.github.huymaster.textguardian.server.data.table

import com.github.huymaster.textguardian.core.entity.ParticipantEntity
import org.ktorm.schema.uuid

object ParticipantTable : BaseTable<ParticipantEntity>("participant") {
    val conversationId = uuid("conversation_id")
        .bindTo { it.conversationId }
    val userId = uuid("user_id")
        .bindTo { it.userId }
}