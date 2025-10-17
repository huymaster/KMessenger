package com.github.huymaster.textguardian.server.net

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Claim
import com.github.huymaster.textguardian.core.dto.UserDTO
import com.github.huymaster.textguardian.server.data.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import java.time.Instant
import java.util.*

const val AUTH_NAME = "jwt"
const val REALM = "KMessenger"
const val ISSUER = "KMessenger"
const val AUDIENCE = "KMessenger"
val JWT_SECRET: String = System.getenv("JWT_SECRET") ?: "KMessenger_JWT_SECRET"

fun Application.configureAuthentication() {
    val userRepository by inject<UserRepository>()
    install(Authentication) {
        jwt(AUTH_NAME) {
            realm = REALM
            verifier(
                JWT.require(Algorithm.HMAC512(JWT_SECRET))
                    .withAudience(AUDIENCE)
                    .withIssuer(ISSUER)
                    .build()
            )
            challenge { defaultScheme, realm ->
                call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
            }
            validate {
                val claim: Claim? = it.payload.getClaim(UserDTO.ID_FIELD)
                val expireTime = it.expiresAt
                if (expireTime == null || Date.from(Instant.now()).after(expireTime)) {
                    respondText("Token expired", status = HttpStatusCode.Unauthorized)
                    return@validate null
                }
                if (claim == null || claim.isNull) {
                    respondText("Unable to retrive user", status = HttpStatusCode.Unauthorized)
                    return@validate null
                } else {
                    val user = runCatching { userRepository.findUserById(UUID.fromString(claim.asString())) }
                    if (user.getOrNull() != null)
                        return@validate JWTPrincipal(it.payload)
                }
            }
        }
    }
}