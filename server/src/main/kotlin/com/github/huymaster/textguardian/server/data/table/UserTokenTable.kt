package com.github.huymaster.textguardian.server.data.table

import com.github.huymaster.textguardian.core.entity.UserTokenEntity
import org.ktorm.schema.boolean
import org.ktorm.schema.timestamp
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object UserTokenTable : BaseTable<UserTokenEntity>("user_token") {
    val id = uuid("user_id")
        .bindTo { it.id }
    val refreshToken = varchar("refresh_token").primaryKey()
        .bindTo { it.refreshToken }
    val deviceInfo = varchar("device_info")
        .bindTo { it.deviceInfo }
    val expiresAt = timestamp("expires_at")
        .bindTo { it.expiresAt }
    val isRevoked = boolean("is_revoked")
        .bindTo { it.isRevoked }
}