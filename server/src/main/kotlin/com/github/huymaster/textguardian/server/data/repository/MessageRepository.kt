package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.api.type.Message
import com.github.huymaster.textguardian.core.entity.MessageEntity
import com.github.huymaster.textguardian.server.data.table.MessageTable
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.mongodb.kotlin.client.model.Filters.eq
import com.mongodb.kotlin.client.model.Filters.`in`
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.koin.core.component.get
import org.koin.core.component.inject
import org.ktorm.dsl.*
import java.time.Instant
import java.util.*

class MessageRepository : BaseRepository<MessageEntity, MessageTable>(MessageTable) {
    data class CipherMessage(val id: String, val cipherText: ByteArray = byteArrayOf()) {
        constructor(uuid: UUID, cipherText: ByteArray) : this(uuid.toString(), cipherText)
    }

    private val _messageCollection: MongoCollection<CipherMessage> by lazy {
        val dabatase: MongoDatabase = get()
        dabatase.getCollection<CipherMessage>("messages")
    }

    private val conversationRepository: ConversationRepository by inject()

    val newMsgFlow = MutableSharedFlow<String>(1)

    suspend fun addMessage(userId: UUID, conversationId: UUID, msg: Message): RepositoryResult {
        try {
            if (!checkPermission(userId, conversationId)) {
                return RepositoryResult.Error(
                    "You are not a participant of this conversation",
                    HttpStatusCode.Forbidden
                )
            }
            val message = MessageEntity()
            message.messageId = UUID.randomUUID()
            message.conversationId = conversationId
            message.senderId = userId
            message.sendAt = Instant.now()
            message.sessionKeys = msg.sessionKeys
            message.replyTo = msg.replyTo
            val result = runCatching { _messageCollection.insertOne(CipherMessage(message.messageId, msg.content)) }
            message.valid = result.isSuccess && msg.sessionKeys.isNotEmpty()
            return if (create(message) != null) {
                conversationRepository.setLastUpdated(conversationId, message.sendAt)
                newMsgFlow.tryEmit(conversationId.toString())
                RepositoryResult.Success(null)
            } else
                RepositoryResult.Error("Failed to add message", HttpStatusCode.InternalServerError)
        } catch (e: Exception) {
            e.printStackTrace()
            return RepositoryResult.Error("Failed to add message. ${e.message}", HttpStatusCode.InternalServerError)
        }
    }

    suspend fun getMessage(
        userId: UUID,
        conversationId: UUID,
        messageId: UUID
    ): RepositoryResult {
        if (!checkPermission(userId, conversationId)) {
            return RepositoryResult.Error(
                "You are not a participant of this conversation",
                HttpStatusCode.Forbidden
            )
        }

        val message = find { (it.messageId eq messageId) and (it.valid eq true) }
            ?: return RepositoryResult.Error("Message not found", HttpStatusCode.NotFound)

        val cipherMessage = _messageCollection.find(CipherMessage::id eq messageId.toString()).firstOrNull()
        val cipherText = cipherMessage?.cipherText
        if (cipherMessage == null || cipherText == null) {
            markInvalidMessage(messageId)
            return RepositoryResult.Error("Message not found", HttpStatusCode.NotFound)
        }
        return RepositoryResult.Success(
            Message(
                id = messageId,
                content = cipherMessage.cipherText,
                senderId = message.senderId,
                sessionKeys = message.sessionKeys,
                sendAt = message.sendAt,
                replyTo = message.replyTo,
                attachments = emptyList()
            )
        )
    }

    suspend fun getMessages(
        userId: UUID,
        conversationId: UUID,
        startMessageId: UUID?,
        limit: Int = 50
    ): RepositoryResult {
        if (!checkPermission(userId, conversationId)) {
            return RepositoryResult.Error(
                "You are not a participant of this conversation",
                HttpStatusCode.Forbidden
            )
        }

        var currentStartTimestamp = if (startMessageId != null) {
            source.newQuery(MessageTable)
                .select(MessageTable.sendAt)
                .where(MessageTable.messageId eq startMessageId)
                .map { it[MessageTable.sendAt] }
                .firstOrNull()
                ?: return RepositoryResult.Error("Start message not found", HttpStatusCode.NotFound)
        } else {
            Instant.now()
        }

        val finalMessageList = mutableListOf<Message>()

        while (finalMessageList.size < limit) {
            val query = source.newQuery(MessageTable)
                .select()
                .where(
                    (MessageTable.conversationId eq conversationId) and
                            (MessageTable.valid eq true) and
                            (MessageTable.sendAt less currentStartTimestamp)
                )
                .orderBy(MessageTable.sendAt.desc())
                .limit(limit)

            val messageEntities = query.map { MessageTable.createEntity(it) }

            if (messageEntities.isEmpty())
                break

            currentStartTimestamp = messageEntities.last().sendAt

            val messageIds = messageEntities.map { it.messageId.toString() }
            val cipherMessages = _messageCollection.find(CipherMessage::id `in` messageIds)
                .toList()
                .associateBy { it.id }

            for (entity in messageEntities) {
                val cipherMessage = cipherMessages[entity.messageId.toString()]
                val cipherText = cipherMessage?.cipherText
                if (cipherMessage != null && cipherText != null) {
                    finalMessageList.add(
                        Message(
                            id = entity.messageId,
                            content = cipherMessage.cipherText,
                            senderId = entity.senderId,
                            sessionKeys = entity.sessionKeys,
                            sendAt = entity.sendAt,
                            replyTo = entity.replyTo,
                            attachments = emptyList()
                        )
                    )
                } else {
                    markInvalidMessage(entity.messageId)
                }

                if (finalMessageList.size == limit) {
                    break
                }
            }

            if (finalMessageList.size == limit) {
                break
            }
        }

        return if (finalMessageList.isEmpty())
            RepositoryResult.Error("No messages found", HttpStatusCode.NotFound)
        else
            RepositoryResult.Success(finalMessageList)
    }

    suspend fun getLastestMessages(userId: UUID, conversationId: UUID, since: UUID): RepositoryResult {
        if (!checkPermission(userId, conversationId)) {
            return RepositoryResult.Error(
                "You are not a participant of this conversation",
                HttpStatusCode.Forbidden
            )
        }

        val currentStartTimestamp =
            source.newQuery(MessageTable)
                .select(MessageTable.sendAt)
                .where(MessageTable.messageId eq since)
                .map { it[MessageTable.sendAt] }
                .firstOrNull()
                ?: return RepositoryResult.Error("Start message not found", HttpStatusCode.NotFound)

        val query = source.newQuery(MessageTable)
            .select()
            .where(
                (MessageTable.conversationId eq conversationId) and
                        (MessageTable.messageId neq since) and
                        (MessageTable.valid eq true) and
                        (MessageTable.sendAt greater currentStartTimestamp)
            )
            .orderBy(MessageTable.sendAt.desc())

        val messageEntities = query.map { MessageTable.createEntity(it) }
        if (messageEntities.isEmpty())
            return RepositoryResult.Error("No messages found", HttpStatusCode.NotFound)

        val messageIds = messageEntities.map { it.messageId.toString() }
        val cipherMessages = _messageCollection.find(CipherMessage::id `in` messageIds)
            .toList()
            .associateBy { it.id }

        val messageList = mutableListOf<Message>()
        for (entity in messageEntities) {
            val cipherMessage = cipherMessages[entity.messageId.toString()]
            val cipherText = cipherMessage?.cipherText
            if (cipherMessage == null || cipherText == null) {
                markInvalidMessage(entity.messageId)
            } else {
                messageList.add(
                    Message(
                        id = entity.messageId,
                        content = cipherMessage.cipherText,
                        senderId = entity.senderId,
                        sessionKeys = entity.sessionKeys,
                        sendAt = entity.sendAt,
                        replyTo = entity.replyTo,
                        attachments = emptyList()
                    )
                )
            }
        }
        return RepositoryResult.Success(messageList)
    }

    private suspend fun markInvalidMessage(messageId: UUID) {
        update({ it.messageId eq messageId }) { it.valid = false }
    }

    private suspend fun checkPermission(userId: UUID, conversationId: UUID): Boolean {
        val repo: ParticipantRepository by inject()
        return repo.isParticipant(conversationId, userId) is RepositoryResult.Success<*>
    }
}