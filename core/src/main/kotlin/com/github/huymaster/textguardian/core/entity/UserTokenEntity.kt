package com.github.huymaster.textguardian.core.entity

import org.ktorm.entity.Entity
import java.time.Instant
import java.util.*


interface UserTokenEntity : BaseEntity<UserTokenEntity> {
    companion object : Entity.Factory<UserTokenEntity>()

    var id: UUID
    var refreshToken: String
    var deviceInfo: String?
    var expiresAt: Instant
    var isRevoked: Boolean
}