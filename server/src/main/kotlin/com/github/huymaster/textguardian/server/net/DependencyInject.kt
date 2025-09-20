package com.github.huymaster.textguardian.server.net

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

private val repositoryModules = module {
}

fun Application.configureDependencyInject() {
    install(Koin) {
        modules(repositoryModules)
    }
}