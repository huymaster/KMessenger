package com.github.huymaster.textguardian.core.entity

import org.ktorm.entity.Entity
import java.util.*

interface PublicKeyEntity : BaseEntity<PublicKeyEntity> {
    companion object : Entity.Factory<PublicKeyEntity>()

    var userId: UUID
    var keys: Array<ByteArray>
}