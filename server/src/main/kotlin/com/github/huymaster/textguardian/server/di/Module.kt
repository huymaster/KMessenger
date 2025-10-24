package com.github.huymaster.textguardian.server.di

import com.github.huymaster.textguardian.server.data.repository.*
import com.github.huymaster.textguardian.server.utils.AttachmentCompressor
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.koin.dsl.module
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.util.concurrent.atomic.AtomicReference

object Module {
    private val _databaseHolder = AtomicReference<Database>()
    private val _mongoDatabaseHolder = AtomicReference<MongoDatabase>()

    private val _database
        get() = Database.connect(
            url = "jdbc:postgresql://api-textguardian.ddns.net:5432/postgres",
            user = "postgres",
            password = System.getenv("POSTGRESQL_PASSWORD"),
            dialect = PostgreSqlDialect()
        )

    val database = module {
        factory {
            val result = runCatching { _databaseHolder.get().useConnection { } }
            if (result.isFailure)
                _databaseHolder.set(_database)
            _databaseHolder.get()
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