package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.ParticipantEntity
import com.github.huymaster.textguardian.server.data.table.ParticipantTable
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import java.util.*

class ParticipantRepository : BaseRepository<ParticipantEntity, ParticipantTable>(ParticipantTable) {
    suspend fun join(conversationId: UUID, userId: UUID): Boolean {
        if (isParticipant(conversationId, userId)) return false
        val entity = ParticipantEntity()
        entity.conversationId = conversationId
        entity.userId = userId
        return create(entity) != null
    }

    suspend fun leave(conversationId: UUID, userId: UUID): Boolean {
        if (!isParticipant(conversationId, userId)) return false
        return delete { ParticipantTable.conversationId eq conversationId and (ParticipantTable.userId eq userId) } > 0
    }

    suspend fun isParticipant(conversationId: UUID, userId: UUID): Boolean {
        return exists { ParticipantTable.conversationId eq conversationId and (ParticipantTable.userId eq userId) }
    }

    suspend fun getParticipantsByConversationId(conversationId: UUID): List<ParticipantEntity> {
        return findAll { ParticipantTable.conversationId eq conversationId }
    }
}