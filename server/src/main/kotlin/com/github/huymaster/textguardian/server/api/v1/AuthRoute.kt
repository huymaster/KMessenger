package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.core.api.type.RegisterRequest
import com.github.huymaster.textguardian.server.api.SubRoute
import io.ktor.server.request.*
import io.ktor.server.routing.*


object AuthRoute : SubRoute() {
    suspend fun register(call: RoutingCall) {
        val request = call.receiveNullable<RegisterRequest>()
        println(request)
    }

    suspend fun login(call: RoutingCall) {
    }

    suspend fun refresh(call: RoutingCall) {}

    suspend fun logout(call: RoutingCall) {}
}