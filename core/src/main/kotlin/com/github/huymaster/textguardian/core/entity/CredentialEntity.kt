package com.github.huymaster.textguardian.core.entity

import org.ktorm.entity.Entity
import java.util.*

interface CredentialEntity : BaseEntity<CredentialEntity> {
    companion object : Entity.Factory<CredentialEntity>()

    var userId: UUID
    var password: ByteArray
    var key: ByteArray
}