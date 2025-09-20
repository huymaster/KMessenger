package com.github.huymaster.textguardian.server.api

import io.ktor.server.response.*
import io.ktor.server.routing.*

object APIVersion1 : BaseAPI(1) {
    override fun register(route: Route) {
        route.get {
            call.respondText("Hello World!")
        }
    }
}