package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.api.type.ConversationInfo
import com.github.huymaster.textguardian.core.entity.ConversationEntity
import com.github.huymaster.textguardian.server.data.table.ConversationTable
import io.ktor.http.*
import org.koin.core.component.inject
import org.ktorm.dsl.eq
import java.time.Instant
import java.util.*

class ConversationRepository : BaseRepository<ConversationEntity, ConversationTable>(ConversationTable) {
    suspend fun getConversationsByUser(userId: UUID): RepositoryResult {
        val participantRepository: ParticipantRepository by inject()
        val conversation = participantRepository.findAll { it.userId eq userId }
            .map { it.conversationId }
            .mapNotNull { cid -> find { it.conversationId eq cid } }
        return RepositoryResult.Success(
            conversation.map { ConversationInfo(it) },
            if (conversation.isEmpty()) HttpStatusCode.NoContent else HttpStatusCode.OK
        )
    }

    suspend fun getConversationById(userId: UUID, conversationId: UUID): RepositoryResult {
        val ownerResult = checkCreator(conversationId, userId)
        if (ownerResult is RepositoryResult.Error)
            return ownerResult
        val conversation = find { it.conversationId eq conversationId }
        if (conversation == null)
            return RepositoryResult.Error("Conversation not found", HttpStatusCode.NotFound)
        return RepositoryResult.Success(conversation)
    }

    suspend fun newConversation(userId: UUID, name: String): RepositoryResult {
        val participantRepository: ParticipantRepository by inject()
        val conversation = ConversationEntity()
        conversation.conversationId = UUID.randomUUID()
        conversation.name = name.trim()
        conversation.createdAt = Instant.now()
        conversation.creator = userId
        return if (create(conversation) != null) {
            participantRepository.addParticipant(conversation.conversationId, userId, userId)
            RepositoryResult.Success(conversation.conversationId)
        } else
            RepositoryResult.Error("Failed to create conversation", HttpStatusCode.InternalServerError)
    }

    suspend fun renameConversation(userId: UUID, conversationId: UUID, name: String): RepositoryResult {
        val ownerResult = checkCreator(conversationId, userId)
        if (ownerResult is RepositoryResult.Error)
            return ownerResult
        return if (update({ it.conversationId eq conversationId }) {
                it.name = name.trim()
                it.lastUpdated = Instant.now()
            } != null)
            RepositoryResult.Success(null)
        else
            RepositoryResult.Error("Failed to rename conversation", HttpStatusCode.InternalServerError)
    }

    suspend fun deleteConversation(userId: UUID, conversationId: UUID): RepositoryResult {
        val ownerResult = checkCreator(conversationId, userId)
        if (ownerResult is RepositoryResult.Error)
            return ownerResult
        return if (delete { it.conversationId eq conversationId } > 0)
            RepositoryResult.Success(null)
        else
            RepositoryResult.Error("Failed to delete conversation", HttpStatusCode.InternalServerError)
    }

    suspend fun checkCreator(conversationId: UUID, userId: UUID): RepositoryResult {
        val conversation = find { it.conversationId eq conversationId }
        if (conversation == null)
            return RepositoryResult.Error("Conversation not found", HttpStatusCode.NotFound)
        if (conversation.creator != userId)
            return RepositoryResult.Error("You are not the owner of this conversation", HttpStatusCode.Forbidden)
        return RepositoryResult.Success(null)
    }
}