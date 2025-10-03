package com.github.huymaster.textguardian.core.entity

import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*

interface UserEntity : BaseEntity<UserEntity> {
    companion object : Entity.Factory<UserEntity>()

    var userId: UUID
    var phoneNumber: String
    var username: String?
    var displayName: String?
    var lastSeen: Instant
    var createdAt: Instant
    var avatarUrl: String?
    var bio: String?
}