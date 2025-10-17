package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.ConversationEntity
import com.github.huymaster.textguardian.server.data.table.ConversationTable
import org.ktorm.dsl.eq
import java.time.Instant
import java.util.*

class ConversationRepository : BaseRepository<ConversationEntity, ConversationTable>(ConversationTable) {
    suspend fun newConversation(name: String): ConversationEntity? {
        val entity = ConversationEntity().apply {
            this.conversationId = UUID.randomUUID()
            this.createdAt = Instant.now()
            this.name = name
        }
        return create(entity)
    }

    suspend fun renameConversation(conversationId: UUID, name: String): Boolean {
        return updateAll({ it.conversationId eq conversationId }, { it.name = name }).isNotEmpty()
    }

    suspend fun getAllConversation(
        userId: UUID,
        participantRepository: ParticipantRepository
    ): List<ConversationEntity> {
        val participants = participantRepository.findAll { it.userId eq userId }
        return participants.mapNotNull { findConversationByConversationId(it.conversationId) }
    }

    suspend fun findConversationByConversationId(conversationId: UUID): ConversationEntity? {
        return find { it.conversationId eq conversationId }
    }

    suspend fun deleteConversation(conversationId: UUID): Boolean {
        return delete { it.conversationId eq conversationId } > 0
    }
}