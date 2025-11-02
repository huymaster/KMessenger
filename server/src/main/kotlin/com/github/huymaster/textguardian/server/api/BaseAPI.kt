package com.github.huymaster.textguardian.server.api

import com.auth0.jwt.interfaces.Claim
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.huymaster.textguardian.server.data.repository.RepositoryResult
import com.github.huymaster.textguardian.server.net.AUTH_NAME
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import kotlinx.html.*
import okhttp3.internal.toImmutableMap
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.ktorm.database.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseAPI protected constructor(val version: Int) : KoinComponent {
    companion object {
        private val rootLogger: Logger = LoggerFactory.getLogger(BaseAPI::class.java)
        private val _API_MAP = mutableMapOf<Int, String>()
        const val DEFAULT_PREFIX = "api"
        val API_LIST get() = _API_MAP.toImmutableMap()
    }

    protected val database: Database by inject()
    protected val mapper: ObjectMapper by inject()
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    init {
        require(version >= 0) { "Version must be non-negative but $version" }
        database.useConnection { it.close() }
        if (!_API_MAP.contains(version))
            _API_MAP[version] = getApiPath()
    }

    fun getApiPath(): String = "$DEFAULT_PREFIX/v$version"
    open suspend fun RoutingContext.swaggerProvider() {
        call.respondHtml {
            head { title { +"Not supported" } }
            body {
                h1 { +"Not supported" }
                p { +"API version $version documentation not available" }
                a(href = "/") { +"Back to home" }
            }
        }
    }

    fun register(routing: Routing) {
        rootLogger.info("Registering API v$version")
        routing.get(getApiPath()) { swaggerProvider() }
        routing.get("${getApiPath()}/health") { call.respond(null, typeInfo<String>()) }
        routing.route(path = getApiPath(), build = { register() })
    }

    protected abstract fun Route.register()

    protected fun Route.protect(route: Route.() -> Unit) {
        authenticate(AUTH_NAME, build = route)
    }

    suspend inline fun <reified T : Any> ApplicationCall.receiveNullableNoThrow(): T? =
        runCatching { this.receive<T>() }.getOrNull()

    suspend inline fun <reified T : Any> ApplicationCall.receive(
        onSuccess: suspend ApplicationCall.(T) -> Unit
    ) {
        runCatching { this.receive<T>() }
            .onSuccess { onSuccess(it) }
            .onFailure { sendErrorResponse("Invalid request", it) }
    }

    suspend inline fun <reified T : Any> ApplicationCall.receive(
        onSuccess: suspend ApplicationCall.(T) -> Unit,
        onFailure: suspend ApplicationCall.(Throwable) -> Unit
    ) {
        runCatching { this.receive<T>() }
            .onSuccess { onSuccess(it) }
            .onFailure { onFailure(it) }
    }

    suspend fun ApplicationCall.sendErrorResponse(
        message: String,
        exception: Throwable? = null,
        status: HttpStatusCode = HttpStatusCode.BadRequest
    ) {
        val exceptionMessage = exception?.message
        if (exceptionMessage != null)
            respondText("$message: $exceptionMessage", status = status)
        else
            respondText(message, status = status)
    }

    suspend fun ApplicationCall.sendErrorResponse(
        error: RepositoryResult
    ) = sendErrorResponse(message = error.message, status = error.desiredStatus)

    suspend fun <T : Any> ApplicationCall.getClaim(claim: String, converter: Claim.() -> T): T? {
        val principal = principal<JWTPrincipal>()
        val claimValue = principal?.payload?.getClaim(claim)
        return claimValue?.let {
            runCatching { converter(it) }
                .onFailure { e -> logger.error("Failed to convert claim $claim", e) }
                .getOrNull()
        }
    }
}