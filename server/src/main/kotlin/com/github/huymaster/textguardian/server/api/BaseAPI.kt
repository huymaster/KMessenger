package com.github.huymaster.textguardian.server.api

import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseAPI protected constructor(val version: Int) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(BaseAPI::class.java)
        const val DEFAULT_PREFIX = "api"
    }

    init {
        require(version > 0) { "Version must be greater than 0 ($version)" }
    }

    fun getApiPath(): String = "$DEFAULT_PREFIX/v$version"

    fun register(routing: Routing) {
        logger.info("Registering API v$version")
        routing.route(getApiPath()) { register(this) }
    }

    protected abstract fun register(route: Route)
}