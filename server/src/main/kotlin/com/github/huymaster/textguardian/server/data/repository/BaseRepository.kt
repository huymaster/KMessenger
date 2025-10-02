package com.github.huymaster.textguardian.server.data.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.huymaster.textguardian.core.entity.BaseEntity
import com.github.huymaster.textguardian.server.data.source.BaseDataSource
import com.github.huymaster.textguardian.server.data.table.BaseTable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ktorm.schema.ColumnDeclaring
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("UNCHECKED_CAST")
abstract class BaseRepository<E : BaseEntity<E>, T : BaseTable<E>>(
    protected val source: BaseDataSource<E, T>
) : KoinComponent {
    protected constructor(table: BaseTable<E>) : this(BaseDataSource(table) as BaseDataSource<E, T>)

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)
    protected val mapper: ObjectMapper by inject()

    suspend fun create(entity: E): E? = source.create(entity)
    suspend fun find(predicate: (T) -> ColumnDeclaring<Boolean>): E? = source.find(predicate)
    suspend fun findAll(predicate: ((T) -> ColumnDeclaring<Boolean>)? = null): List<E> = source.findAll(predicate)
    suspend fun update(predicate: (T) -> ColumnDeclaring<Boolean>, updater: (E) -> Unit): E? =
        source.update(predicate, updater)

    suspend fun updateAll(predicate: ((T) -> ColumnDeclaring<Boolean>)? = null, updater: (E) -> Unit): List<E> =
        source.updateAll(predicate, updater)

    suspend fun delete(predicate: (T) -> ColumnDeclaring<Boolean>): Int = source.delete(predicate)
    suspend fun count(predicate: ((T) -> ColumnDeclaring<Boolean>)? = null): Int = source.count(predicate)
    suspend fun exists(predicate: (T) -> ColumnDeclaring<Boolean>): Boolean = source.exists(predicate)
}