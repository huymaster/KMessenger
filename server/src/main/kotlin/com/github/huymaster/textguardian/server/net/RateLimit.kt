package com.github.huymaster.textguardian.server.net

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.seconds

fun Application.configureRateLimit() {
    install(RateLimit) {
        global {
            rateLimiter(
                limit = 10,
                refillPeriod = 1.seconds,
                initialSize = 0
            )
        }
    }
}