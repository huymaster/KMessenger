package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.MessageEntity
import com.github.huymaster.textguardian.server.data.table.MessageTable
import org.ktorm.dsl.*
import java.time.Instant
import java.util.*

class MessageRepository : BaseRepository<MessageEntity, MessageTable>(MessageTable) {
    suspend fun newMessage(
        conversationId: UUID,
        senderId: UUID,
        sessionKeys: Array<ByteArray>,
        replyTo: UUID? = null
    ): MessageEntity? {
        val entity = MessageEntity().apply {
            this.messageId = UUID.randomUUID()
            this.conversationId = conversationId
            this.senderId = senderId
            this.sendAt = Instant.now()
            this.sessionKeys = sessionKeys
            this.replyTo = replyTo
        }
        return create(entity)
    }

    suspend fun findAllMessagesByConversationId(
        conversationId: UUID,
        limit: Int = 30,
        offset: Int = 0
    ): List<MessageEntity> {
        val query = source.newQuery(MessageTable).select()
            .where { MessageTable.conversationId eq conversationId }
            .orderBy(MessageTable.sendAt.desc())
            .limit(limit)
            .offset(offset)
        return query.map { MessageTable.createEntity(it) }
    }

    suspend fun findMessageByMessageId(messageId: UUID): MessageEntity? {
        return find { it.messageId eq messageId }
    }

    suspend fun deleteMessageByMessageId(messageId: UUID): Boolean {
        return delete { it.messageId eq messageId } > 0
    }

    suspend fun deleteAllMessagesByConversationId(conversationId: UUID): Boolean {
        return delete { it.conversationId eq conversationId } > 0
    }
}