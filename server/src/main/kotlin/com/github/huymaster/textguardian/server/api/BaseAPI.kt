package com.github.huymaster.textguardian.server.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.huymaster.textguardian.server.net.AUTH_NAME
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseAPI protected constructor(val version: Int) : KoinComponent {
    companion object {
        private val rootLogger: Logger = LoggerFactory.getLogger(BaseAPI::class.java)
        const val DEFAULT_PREFIX = "api"
    }

    protected val database: Database by inject()
    protected val mapper: ObjectMapper by inject()
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    init {
        require(version >= 0) { "Version must be non-negative but $version" }
        database.useConnection { it.close() }
    }

    fun getApiPath(): String = "$DEFAULT_PREFIX/v$version"

    fun register(routing: Routing) {
        rootLogger.info("Registering API v$version")
        routing.route(path = getApiPath(), build = { register() })
    }

    protected abstract fun Route.register()

    protected fun Route.protect(route: Route.() -> Unit) {
        authenticate(AUTH_NAME) { route() }
    }

    suspend inline fun <reified T : Any> RoutingCall.receive(
        onSuccess: suspend RoutingCall.(T) -> Unit
    ) {
        runCatching { this.receive<T>() }
            .onSuccess { onSuccess(it) }
            .onFailure { sendErrorResponse(this, "Invalid request", it) }
    }

    suspend inline fun <reified T : Any> RoutingCall.receive(
        onSuccess: suspend RoutingCall.(T) -> Unit,
        onFailure: suspend RoutingCall.(Throwable) -> Unit
    ) {
        runCatching { this.receive<T>() }
            .onSuccess { onSuccess(it) }
            .onFailure { onFailure(it) }
    }

    suspend fun sendErrorResponse(
        call: RoutingCall,
        message: String,
        exception: Throwable? = null,
        status: HttpStatusCode = HttpStatusCode.BadRequest
    ) {
        val exceptionMessage = exception?.message
        if (exceptionMessage != null)
            call.respondText("$message: $exceptionMessage", status = status)
        else
            call.respondText(message, status = status)
    }
}