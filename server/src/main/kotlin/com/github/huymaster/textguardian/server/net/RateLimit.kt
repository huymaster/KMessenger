package com.github.huymaster.textguardian.server.net

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.hours

fun Application.configureRateLimit() {
    install(RateLimit) {
        global {
            rateLimiter(
                limit = 600 * 60,
                refillPeriod = 1.hours,
                initialSize = 0
            )
        }
    }
}