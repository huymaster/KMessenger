package com.github.huymaster.textguardian.server.api

import com.github.huymaster.textguardian.server.api.v1.AuthRoute
import io.ktor.server.routing.*

object APIVersion1 : BaseAPI(1) {
    override fun Route.register() {
        authRoute()
    }

    private fun Route.authRoute() {
        post("/auth/register") { AuthRoute.register(call) }
        post("/auth/login") { AuthRoute.login(call) }
        protect { get("/auth/refresh") { AuthRoute.refresh(call) } }
        protect { delete("/auth/logout") { AuthRoute.logout(call) } }
    }
}