package com.github.huymaster.textguardian.server.data.table

import com.github.huymaster.textguardian.core.entity.CredentialEntity
import org.ktorm.schema.bytes
import org.ktorm.schema.uuid

object CredentialTable : BaseTable<CredentialEntity>("credential") {
    val userId = uuid("user_id").primaryKey()
        .bindTo { it.userId }
    val password = bytes("password")
        .bindTo { it.password }
    val key = bytes("key")
        .bindTo { it.key }
}