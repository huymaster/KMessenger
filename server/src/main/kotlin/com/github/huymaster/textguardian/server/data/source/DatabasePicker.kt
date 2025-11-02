package com.github.huymaster.textguardian.server.data.source

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.measureTimedValue

class DatabasePicker {
    private val logger = LoggerFactory.getLogger(DatabasePicker::class.java)

    private data class DBConnectionInfo(
        val factory: DatabaseFactory,
        val database: Database,
        val latency: Duration
    )

    class DatabaseFactory(val domain: String) {
        operator fun invoke(): Database = Database.connect(
            url = "jdbc:postgresql://$domain:5432/postgres",
            user = "huymaster",
            password = System.getenv("POSTGRESQL_PASSWORD"),
            dialect = PostgreSqlDialect()
        )
    }

    private val domains
        get() = listOf("localhost", "127.0.0.1", "::1", "api-textguardian.ddns.net").shuffled()
    private val databaseFactories
        get() = domains.map { DatabaseFactory(it) }.shuffled()
    private var cachedDatabaseConnection: DBConnectionInfo? = null

    private fun checkConnection(factory: DatabaseFactory) =
        runCatching { checkConnection(factory()) }.getOrNull()

    private fun checkConnection(database: Database?): Database? =
        runCatching { database?.apply { useConnection { it.close() } } }.getOrNull()

    suspend fun pickFastest() = supervisorScope {
        val channel = Channel<DBConnectionInfo>(1)
        databaseFactories.forEach { factory ->
            launch {
                try {
                    val result = measureTimedValue { checkConnection(factory) }
                    result.value?.let { db ->
                        val info = DBConnectionInfo(factory, db, result.duration)
                        channel.send(info)
                    }
                } catch (_: Exception) {
                }
            }
        }
        cachedDatabaseConnection?.let { cachedInfo ->
            launch {
                try {
                    val result = measureTimedValue { checkConnection(cachedInfo.factory) }
                    result.value?.let { db ->
                        val updatedInfo = DBConnectionInfo(cachedInfo.factory, db, result.duration)
                        channel.send(updatedInfo)
                    }
                } catch (_: Exception) {
                }
            }
        }

        val fastest = channel.receive()
        coroutineContext.cancelChildren()

        if (cachedDatabaseConnection == null || fastest.latency < cachedDatabaseConnection!!.latency) {
            cachedDatabaseConnection = fastest
            useLogger { info("Connected to fastest connection: ${fastest.factory.domain} with latency ${fastest.latency}") }
        }

        channel.close()
        fastest.database
    }

    fun pickFastestAsync() = runBlocking { pickFastest() }

    private fun useLogger(block: Logger.() -> Unit) =
        Thread { logger.block() }.apply { name = "DatabasePicker" }.start()
}