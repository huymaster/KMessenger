package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.ParticipantEntity
import com.github.huymaster.textguardian.server.data.table.ParticipantTable
import io.ktor.http.*
import org.koin.core.component.inject
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import java.util.*

class ParticipantRepository : BaseRepository<ParticipantEntity, ParticipantTable>(ParticipantTable) {
    suspend fun getParticipantsByConversation(conversationId: UUID): RepositoryResult {
        return RepositoryResult.Success(findAll { it.conversationId eq conversationId })
    }

    suspend fun isParticipant(conversationId: UUID, userId: UUID): RepositoryResult {
        if (find { (it.conversationId eq conversationId) and (it.userId eq userId) } != null)
            return RepositoryResult.Success(Unit)
        return RepositoryResult.Error("User is not a participant of this conversation", HttpStatusCode.Forbidden)
    }

    suspend fun addParticipant(conversationId: UUID, inviterId: UUID, userId: UUID): RepositoryResult {
        val conversationRepository: ConversationRepository by inject()
        val ownerResult = conversationRepository.checkCreator(conversationId, inviterId)
        if (ownerResult is RepositoryResult.Error)
            return ownerResult
        val participant = ParticipantEntity()
        participant.conversationId = conversationId
        participant.userId = userId
        return if (create(participant) != null)
            RepositoryResult.Success(null)
        else
            RepositoryResult.Error("Failed to add participant", HttpStatusCode.InternalServerError)
    }

    suspend fun removeParticipant(conversationId: UUID, userId: UUID): RepositoryResult {
        val participant = find { (it.conversationId eq conversationId) and (it.userId eq userId) }
        if (participant == null)
            return RepositoryResult.Error("Participant not found", HttpStatusCode.NotFound)
        return if (delete { it.conversationId eq conversationId } > 0)
            RepositoryResult.Success(null)
        else
            RepositoryResult.Error("Failed to remove participant", HttpStatusCode.InternalServerError)
    }
}