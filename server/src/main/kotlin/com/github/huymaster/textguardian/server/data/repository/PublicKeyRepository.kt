package com.github.huymaster.textguardian.server.data.repository

import com.github.huymaster.textguardian.core.api.type.UserPublicKey
import com.github.huymaster.textguardian.core.api.type.UserPublicKeys
import com.github.huymaster.textguardian.core.entity.PublicKeyEntity
import com.github.huymaster.textguardian.server.data.table.PublicKeyTable
import io.ktor.http.*
import org.koin.core.component.get
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class PublicKeyRepository : BaseRepository<PublicKeyEntity, PublicKeyTable>(PublicKeyTable) {
    suspend fun getPublicKeys(userId: UUID): RepositoryResult {
        try {
            val list = findAll { it.userId eq userId }
                .filter { it.shouldRemove > Instant.now() }
                .map { runCatching { UserPublicKey(it.userId, it.key) } }
                .mapNotNull { it.getOrNull() }
            if (list.isEmpty()) return RepositoryResult.Error("No public keys found", HttpStatusCode.NotFound)
            val pkList = UserPublicKeys.from(list)
            return RepositoryResult.Success(pkList)
        } catch (e: Exception) {
            return RepositoryResult.Error("Failed to get public keys. ${e.message}", HttpStatusCode.InternalServerError)
        }
    }

    suspend fun addPublicKey(userId: UUID, key: String): RepositoryResult {

        if (extendTimeIfExist(userId, key)) return RepositoryResult.Success(null)
        try {
            val publicKey = PublicKeyEntity()
            publicKey.userId = userId
            publicKey.key = get<Base64.Decoder>().decode(key)
            return if (create(publicKey) != null)
                RepositoryResult.Success(null)
            else
                RepositoryResult.Error("Failed to add public key", HttpStatusCode.InternalServerError)
        } catch (e: Exception) {
            return RepositoryResult.Error("Failed to add public key. ${e.message}", HttpStatusCode.InternalServerError)
        }
    }

    private suspend fun extendTimeIfExist(userId: UUID, key: String): Boolean {
        val bytes = get<Base64.Decoder>().decode(key)
        if (!exists { (it.userId eq userId) and (it.key eq bytes) }) return false
        val updated = updateAll(
            { (it.userId eq userId) and (it.key eq bytes) },
            { it.shouldRemove = Instant.now().plus(1L, ChronoUnit.DAYS) }
        )

        return updated.isNotEmpty()
    }
}