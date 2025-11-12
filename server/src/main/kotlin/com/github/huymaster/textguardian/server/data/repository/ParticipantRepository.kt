package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.api.type.ParticipantInfo
import com.github.huymaster.textguardian.core.entity.ParticipantEntity
import com.github.huymaster.textguardian.server.data.table.ParticipantTable
import io.ktor.http.*
import org.koin.core.component.inject
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import java.util.*

class ParticipantRepository : BaseRepository<ParticipantEntity, ParticipantTable>(ParticipantTable) {
    suspend fun getParticipantsByConversation(conversationId: String): RepositoryResult {
        val conversationId = runCatching {
            UUID.fromString(conversationId)
        }.onFailure {
            return RepositoryResult.Error("Invalid conversationId", HttpStatusCode.BadRequest)
        }.getOrNull()!!
        return getParticipantsByConversation(conversationId)
    }

    suspend fun getParticipantsByConversation(conversationId: UUID): RepositoryResult {
        return RepositoryResult.Success(findAll { it.conversationId eq conversationId }.map { ParticipantInfo(it) })
    }

    suspend fun isParticipant(conversationId: String, userId: String): RepositoryResult {
        val conversationId = runCatching {
            UUID.fromString(conversationId)
        }.onFailure {
            return RepositoryResult.Error("Invalid conversationId", HttpStatusCode.BadRequest)
        }.getOrNull()!!
        val userId = runCatching {
            UUID.fromString(userId)
        }.onFailure {
            return RepositoryResult.Error("Invalid userId", HttpStatusCode.BadRequest)
        }.getOrNull()!!
        return isParticipant(conversationId, userId)
    }

    suspend fun isParticipant(conversationId: UUID, userId: UUID): RepositoryResult {
        if (find { (it.conversationId eq conversationId) and (it.userId eq userId) } != null)
            return RepositoryResult.Success(Unit)
        return RepositoryResult.Error("User is not a participant of this conversation", HttpStatusCode.Forbidden)
    }

    suspend fun addParticipant(conversationId: String, inviterId: String, userId: String): RepositoryResult {
        val conversationId = runCatching {
            UUID.fromString(conversationId)
        }.onFailure {
            return RepositoryResult.Error("Invalid conversationId", HttpStatusCode.BadRequest)
        }.getOrNull()!!
        val inviterId = runCatching {
            UUID.fromString(inviterId)
        }.onFailure {
            return RepositoryResult.Error("Invalid inviterId", HttpStatusCode.BadRequest)
        }.getOrNull()!!
        val userId = runCatching {
            UUID.fromString(userId)
        }.onFailure {
            return RepositoryResult.Error("Invalid userId", HttpStatusCode.BadRequest)
        }.getOrNull()!!
        return addParticipant(conversationId, inviterId, userId)
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

    suspend fun removeParticipant(conversationId: String, userId: String): RepositoryResult {
        val conversationId = runCatching {
            UUID.fromString(conversationId)
        }.onFailure {
            return RepositoryResult.Error("Invalid conversationId", HttpStatusCode.BadRequest)
        }.getOrNull()!!
        val userId = runCatching {
            UUID.fromString(userId)
        }.onFailure {
            return RepositoryResult.Error("Invalid userId", HttpStatusCode.BadRequest)
        }.getOrNull()!!
        return removeParticipant(conversationId, userId)
    }

    suspend fun removeParticipant(conversationId: UUID, userId: UUID): RepositoryResult {
        val participant = find { (it.conversationId eq conversationId) and (it.userId eq userId) }
        if (participant == null)
            return RepositoryResult.Error("Participant not found", HttpStatusCode.NotFound)
        return if (delete { (it.conversationId eq conversationId) and (it.userId eq userId) } > 0)
            RepositoryResult.Success(null)
        else
            RepositoryResult.Error("Failed to remove participant", HttpStatusCode.InternalServerError)
    }
}