package com.github.huymaster.textguardian.server.data.table

import com.github.huymaster.textguardian.core.entity.UserEntity
import org.ktorm.schema.timestamp
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object UserTable : BaseTable<UserEntity>("user") {
    val userId = uuid("user_id").primaryKey()
        .bindTo { it.userId }
    val phoneNumber = varchar("phone_number")
        .bindTo { it.phoneNumber }
    val username = varchar("username")
        .bindTo { it.username }
    val displayName = varchar("display_name")
        .bindTo { it.displayName }
    val lastSeen = timestamp("last_seen")
        .bindTo { it.lastSeen }
    val createdAt = timestamp("created_at")
        .bindTo { it.createdAt }
    val avartarUrl = varchar("avatar")
        .bindTo { it.avatarUrl }
    val bio = varchar("bio")
        .bindTo { it.bio }
}