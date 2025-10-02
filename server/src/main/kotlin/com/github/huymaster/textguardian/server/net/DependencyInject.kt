package com.github.huymaster.textguardian.server.net

import com.github.huymaster.textguardian.core.di.SharedModule
import com.github.huymaster.textguardian.server.di.Module
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin


fun Application.configureDependencyInject() {
    install(Koin) {
        modules(SharedModule.objectMapper)
        modules(Module.database)
    }
}