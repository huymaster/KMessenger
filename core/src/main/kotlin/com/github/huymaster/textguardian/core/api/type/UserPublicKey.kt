package com.github.huymaster.textguardian.core.api.type

import java.util.*

data class UserPublicKey(
    val userId: UUID,
    val key: Array<Byte>
) {
    constructor(userId: UUID, key: ByteArray) : this(userId, key.toTypedArray())
}