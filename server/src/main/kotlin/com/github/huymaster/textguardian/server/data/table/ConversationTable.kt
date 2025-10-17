package com.github.huymaster.textguardian.server.data.table

import com.github.huymaster.textguardian.core.entity.ConversationEntity
import org.ktorm.schema.timestamp
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object ConversationTable : BaseTable<ConversationEntity>("conversation") {
    val conversationId = uuid("conversation_id").primaryKey()
        .bindTo { it.conversationId }
    val createdAt = timestamp("created_at")
        .bindTo { it.createdAt }
    val name = varchar("name")
        .bindTo { it.name }
    val creator = uuid("creator")
        .bindTo { it.creator }
}