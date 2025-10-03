package com.github.huymaster.textguardian.server.data.table

import com.github.huymaster.textguardian.core.entity.PublicKeyEntity
import com.github.huymaster.textguardian.core.utils.bytesarray
import org.ktorm.schema.uuid

object PublicKeyTable : BaseTable<PublicKeyEntity>("public_key") {
    val userId = uuid("user_id").primaryKey()
        .bindTo { it.userId }
    val keys = bytesarray("keys")
        .bindTo { it.keys }
}