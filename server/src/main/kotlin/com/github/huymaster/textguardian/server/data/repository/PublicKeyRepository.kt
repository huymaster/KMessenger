package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.entity.PublicKeyEntity
import com.github.huymaster.textguardian.server.data.table.PublicKeyTable
import org.ktorm.dsl.eq
import java.util.*

class PublicKeyRepository : BaseRepository<PublicKeyEntity, PublicKeyTable>(PublicKeyTable) {
    suspend fun getPublicKeyByUserId(userId: UUID): Array<ByteArray>? {
        return find { it.userId eq userId }?.keys
    }

    suspend fun createPublicKey(userId: UUID, key: ByteArray): PublicKeyEntity? {
        runCatching {
            val entity = find { it.userId eq userId }
            if (entity == null) {
                create(PublicKeyEntity().apply {
                    this.userId = userId
                    this.keys = emptyArray()
                })
            }
        }.onFailure { throw it }
        return update({ it.userId eq userId }) {
            if (it.keys.contains(key)) return@update
            it.keys += key
        }
    }
}