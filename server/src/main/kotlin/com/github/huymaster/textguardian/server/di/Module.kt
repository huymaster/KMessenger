package com.github.huymaster.textguardian.server.di

import com.github.huymaster.textguardian.server.data.repository.CredentialRepository
import com.github.huymaster.textguardian.server.data.repository.UserRepository
import com.github.huymaster.textguardian.server.data.repository.UserTokenRepository
import org.koin.dsl.module
import org.ktorm.database.Database
import java.util.concurrent.atomic.AtomicReference

object Module {
    private val _databaseHolder = AtomicReference<Database>()
    private val _database
        get() = Database.connect(
            url = "jdbc:postgresql://localhost:5432/postgres",
            user = "postgres",
            password = System.getenv("POSTGRESQL_PASSWORD")
        )
    val database = module {
        factory {
            val result = runCatching { _databaseHolder.get().useConnection { } }
            if (result.isFailure)
                _databaseHolder.set(_database)
            _databaseHolder.get()
        }
        single { UserRepository() }
        single { CredentialRepository() }
        single { UserTokenRepository() }
    }
}