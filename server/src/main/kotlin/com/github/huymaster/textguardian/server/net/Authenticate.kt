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
import org.ktorm.dsl.eq
import java.time.Instant
import java.util.*

const val AUTH_NAME = "jwt"
const val REALM = "KMessenger"
val JWT_SECRET: String = System.getenv("JWT_SECRET") ?: "KMessenger_JWT_SECRET"

fun Application.configureAuthentication() {
    val repository by inject<UserRepository>()
    install(Authentication) {
        jwt(AUTH_NAME) {
            realm = REALM
            verifier(
                JWT.require(Algorithm.HMAC512(JWT_SECRET))
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
                    val uuid = runCatching { UUID.fromString(claim.asString()) }.getOrNull()
                    if (uuid == null) {
                        respondText("Invalid user id", status = HttpStatusCode.Unauthorized)
                        return@validate null
                    }
                    val exists = repository.exists { e -> e.userId eq uuid }
                    if (exists)
                        return@validate uuid
                    else {
                        respondText("User not found", status = HttpStatusCode.Unauthorized)
                        return@validate null
                    }
                }
            }
        }
    }
}