package com.github.huymaster.textguardian.server.data.table

import com.github.huymaster.textguardian.core.entity.MessageEntity
import com.github.huymaster.textguardian.core.utils.bytesarray
import org.ktorm.schema.timestamp
import org.ktorm.schema.uuid

object MessageTable : BaseTable<MessageEntity>("message") {
    val messageId = uuid("message_id").primaryKey()
        .bindTo { it.messageId }
    val conversationId = uuid("conversation_id")
        .bindTo { it.conversationId }
    val senderId = uuid("sender_id")
        .bindTo { it.senderId }
    val sendAt = timestamp("send_at")
        .bindTo { it.sendAt }
    val sessionKeys = bytesarray("session_keys")
        .bindTo { it.sessionKeys }
    val replyTo = uuid("reply_to")
        .bindTo { it.replyTo }
}