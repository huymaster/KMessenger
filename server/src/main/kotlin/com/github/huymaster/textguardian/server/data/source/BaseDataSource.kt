package com.github.huymaster.textguardian.server.data.source

import com.github.huymaster.textguardian.core.entity.BaseEntity
import com.github.huymaster.textguardian.server.data.table.BaseTable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.ktorm.entity.*
import org.ktorm.schema.ColumnDeclaring

abstract class BaseDataSource<E : BaseEntity<E>, T : BaseTable<E>> : KoinComponent {
    companion object {
        operator fun <E : BaseEntity<E>, T : BaseTable<E>> invoke(table: T): BaseDataSource<E, T> {
            return invoke { it.sequenceOf(table) }
        }

        operator fun <E : BaseEntity<E>, T : BaseTable<E>> invoke(table: (Database) -> EntitySequence<E, T>): BaseDataSource<E, T> {
            return new(table)
        }

        fun <E : BaseEntity<E>, T : BaseTable<E>> new(table: T): BaseDataSource<E, T> {
            return new { it.sequenceOf(table) }
        }

        fun <E : BaseEntity<E>, T : BaseTable<E>> new(
            table: (Database) -> EntitySequence<E, T>
        ): BaseDataSource<E, T> {
            return object : BaseDataSource<E, T>() {
                override val table: EntitySequence<E, T> get() = table(database)
            }
        }
    }

    protected val database: Database by inject()
    protected abstract val table: EntitySequence<E, T>

    open suspend fun create(entity: E): E? =
        if (table.add(entity) > 0) entity else null

    open suspend fun find(predicate: (T) -> ColumnDeclaring<Boolean>): E? =
        table.find(predicate)

    open suspend fun findAll(predicate: ((T) -> ColumnDeclaring<Boolean>)? = null): List<E> =
        if (predicate == null) table.toList() else table.filter(predicate).toList()

    open suspend fun update(predicate: (T) -> ColumnDeclaring<Boolean>, updater: (E) -> Unit): E? {
        if (exists(predicate)) {
            val entity = find(predicate)!!
            updater(entity)
            return entity
        } else return null
    }

    open suspend fun updateAll(predicate: ((T) -> ColumnDeclaring<Boolean>)? = null, updater: (E) -> Unit): List<E> {
        val entities = findAll(predicate)
        entities.forEach { updater(it) }
        return entities
    }

    open suspend fun delete(predicate: (T) -> ColumnDeclaring<Boolean>): Int = table.removeIf(predicate)
    open suspend fun count(predicate: ((T) -> ColumnDeclaring<Boolean>)? = null): Int =
        if (predicate == null) table.count() else table.count(predicate)

    open suspend fun exists(predicate: (T) -> ColumnDeclaring<Boolean>): Boolean = count(predicate) > 0
}