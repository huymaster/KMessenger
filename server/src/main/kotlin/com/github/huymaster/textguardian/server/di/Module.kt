package com.github.huymaster.textguardian.server.di

import com.github.huymaster.textguardian.server.data.repository.*
import com.github.huymaster.textguardian.server.utils.AttachmentCompressor
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.koin.dsl.module
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

object Module {
    private val _databaseHolder = AtomicReference<Database>()
    private val _mongoDatabaseHolder = AtomicReference<MongoDatabase>()

    private val _database
        get() = Database.connect(
            url = "jdbc:postgresql://localhost:5432/postgres",
            user = "postgres",
            password = System.getenv("POSTGRESQL_PASSWORD"),
            dialect = PostgreSqlDialect()
        )
    private val settings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(System.getenv("MONGO_URI")))
        .applyToSocketSettings { it.connectTimeout(5, TimeUnit.SECONDS) }
        .build()
    private val _mongoDatabase
        get() = MongoClient.create(settings).getDatabase("kmessenger")

    val database = module {
        factory {
            val result = runCatching { _databaseHolder.get().useConnection { } }
            if (result.isFailure)
                _databaseHolder.set(_database)
            _databaseHolder.get()
        }
        factory {
            val client = MongoClient.create(settings)
            val result = runCatching { _mongoDatabaseHolder.get().listCollections() }
            if (result.isFailure)
                _mongoDatabaseHolder.set(_mongoDatabase)
            _mongoDatabaseHolder.get()
        }
        single { AttachmentRepository() }
        single { ConversationRepository() }
        single { CredentialRepository() }
        single { MessageAttachmentRepository() }
        single { MessageRepository() }
        single { ParticipantRepository() }
        single { PublicKeyRepository() }
        single { UserRepository() }
        single { UserTokenRepository() }
    }
    val utils = module {
        single { AttachmentCompressor() }
    }
}