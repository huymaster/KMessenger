package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.MessageEntity
import com.github.huymaster.textguardian.server.data.table.MessageTable
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.util.*
import org.koin.core.component.get
import java.util.*

class MessageRepository : BaseRepository<MessageEntity, MessageTable>(MessageTable) {
    private class CipherMessage(val id: UUID, val cipherText: String) {
        constructor(id: UUID, cipherText: ByteArray) : this(id, cipherText.encodeBase64())

        val bytes: ByteArray get() = cipherText.decodeBase64Bytes()
    }

    private val _messageCollection: MongoCollection<CipherMessage> by lazy {
        val dabatase: MongoDatabase = get()
        dabatase.getCollection<CipherMessage>("messages")
    }
}