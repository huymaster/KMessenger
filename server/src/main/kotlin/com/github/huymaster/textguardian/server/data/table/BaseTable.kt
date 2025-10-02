package com.github.huymaster.textguardian.server.data.table

import org.ktorm.entity.Entity
import org.ktorm.schema.Table

abstract class BaseTable<E : Entity<E>>(
    tableName: String
) : Table<E>(
    tableName = tableName,
    schema = "kmessenger"
)