package com.github.huymaster.textguardian.server.di

import com.github.huymaster.textguardian.server.data.repository.*
import com.github.huymaster.textguardian.server.data.source.DatabasePicker
import com.github.huymaster.textguardian.server.utils.AttachmentCompressor
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicReference

object Module {
    private val _mongoDatabaseHolder = AtomicReference<MongoDatabase>()
    private val _pojoProvider = PojoCodecProvider.builder()
        .automatic(true)
        .build()
    private val _codecRegistry = fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        fromProviders(_pojoProvider)
    )
    private val _settings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(System.getenv("MONGODB_URI")))
        .uuidRepresentation(UuidRepresentation.STANDARD)
        .codecRegistry(_codecRegistry)
        .build()

    val database = module {
        factory {
            if (_mongoDatabaseHolder.get() == null) {
                _mongoDatabaseHolder.set(MongoClient.create(_settings).getDatabase("kmessenger"))
            }
            _mongoDatabaseHolder.get()
        }
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