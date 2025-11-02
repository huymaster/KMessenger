package com.github.huymaster.textguardian.server.di

import com.github.huymaster.textguardian.server.data.repository.*
import com.github.huymaster.textguardian.server.data.source.DatabasePicker
import com.github.huymaster.textguardian.server.utils.AttachmentCompressor
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.koin.dsl.module
import org.ktorm.database.Database
import java.util.concurrent.atomic.AtomicReference

object Module {
    private val _databaseHolder = AtomicReference<Database>()
    private val _mongoDatabaseHolder = AtomicReference<MongoDatabase>()

    val database = module {
        single { DatabasePicker() }
        factory { get<DatabasePicker>().pickFastestAsync() }
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