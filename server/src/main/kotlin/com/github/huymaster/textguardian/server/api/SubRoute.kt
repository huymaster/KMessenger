package com.github.huymaster.textguardian.server.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class SubRoute : KoinComponent {
    protected val database: Database by inject()
    protected val mapper: ObjectMapper by inject()
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    init {
        database.useConnection { it.close() }
    }
}