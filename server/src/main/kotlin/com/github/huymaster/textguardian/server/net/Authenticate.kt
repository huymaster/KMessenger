package com.github.huymaster.textguardian.server.net

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Claim
import com.github.huymaster.textguardian.server.data.repository.RepositoryResult
import com.github.huymaster.textguardian.server.data.repository.UserRepository
import com.github.huymaster.textguardian.server.data.repository.UserTokenRepository
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

val ALGORITHM: Algorithm = Algorithm.HMAC256(JWT_SECRET)

const val JWT_EXPIRE_MIN = 10L
const val REFRESH_TOKEN_CLAIM = "refreshToken"
const val USER_ID_CLAIM = "userId"

fun Application.configureAuthentication() {
    val uRep by inject<UserRepository>()
    val tRep by inject<UserTokenRepository>()
    install(Authentication) {
        jwt(AUTH_NAME) {
            realm = REALM
            verifier(JWT.require(ALGORITHM).build())
            challenge { defaultScheme, realm ->
                call.response.status(HttpStatusCode.Unauthorized)
                call.respondText("Unauthorized")
            }
            validate {
                val uClaim: Claim? = it.payload.getClaim(USER_ID_CLAIM)
                val rClaim: Claim? = it.payload.getClaim(REFRESH_TOKEN_CLAIM)
                val expireTime = it.expiresAt
                if (expireTime == null || Date.from(Instant.now()).after(expireTime)) {
                    respondText("Token expired", status = HttpStatusCode.Unauthorized)
                    null
                } else
                    if (uClaim == null || uClaim.isNull) {
                        respondText("Unable to retrive user", status = HttpStatusCode.Unauthorized)
                        null
                    } else if (rClaim == null || rClaim.isNull) {
                        respondText("Unable to retrive refresh token", status = HttpStatusCode.Unauthorized)
                        null
                    } else {
                        val uuid = runCatching { UUID.fromString(uClaim.asString()) }.getOrNull()
                        if (uuid == null) {
                            respondText("Invalid user id", status = HttpStatusCode.Unauthorized)
                            return@validate null
                        }
                        val uE = uRep.exists { e -> e.userId eq uuid }
                        val tV = tRep.checkToken(rClaim.asString())

                        if (!uE)
                            respondText("User not found", status = HttpStatusCode.Unauthorized)
                        else if (tV is RepositoryResult.Error)
                            respondText(tV.message, status = tV.desiredStatus)
                        else
                            JWTPrincipal(it.payload)
                    }
            }
        }
    }
}