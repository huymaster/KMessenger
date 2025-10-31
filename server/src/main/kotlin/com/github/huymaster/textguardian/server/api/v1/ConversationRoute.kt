package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.server.api.SubRoute
import io.ktor.server.response.*
import io.ktor.server.routing.*

object ConversationRoute : SubRoute() {
    suspend fun getConversations(call: RoutingCall) {
        call.respondText("Hello World!")
    }
}