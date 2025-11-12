package com.github.huymaster.textguardian.server.data.table

import com.github.huymaster.textguardian.core.entity.PublicKeyEntity
import org.ktorm.schema.bytes
import org.ktorm.schema.timestamp
import org.ktorm.schema.uuid

object PublicKeyTable : BaseTable<PublicKeyEntity>("public_key") {
    val userId = uuid("user_id").primaryKey()
        .bindTo { it.userId }
    val key = bytes("key")
        .bindTo { it.key }
    val shouldRemove = timestamp("should_remove")
        .bindTo { it.shouldRemove }
}