package com.github.huymaster.textguardian.server.data.table

import com.github.huymaster.textguardian.core.entity.CredentialEntity
import org.ktorm.schema.bytes
import org.ktorm.schema.uuid

object CredentialTable : BaseTable<CredentialEntity>("credential") {
    val id = uuid("user_id").primaryKey()
        .bindTo { it.id }
    val password = bytes("password")
        .bindTo { it.password }
    val key = bytes("key")
        .bindTo { it.key }
}