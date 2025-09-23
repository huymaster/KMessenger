package com.github.huymaster.textguardian.server.api

import com.github.huymaster.textguardian.core.dto.UserDTO
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

object APIVersion1 : BaseAPI(1) {
    override fun register(route: Route) {
        route.get("/user/byphone/{number}") {
            val regex = Regex("^0\\d{9}$")
            val number = call.parameters["number"]
            if (number == null || !regex.matches(number))
                call.respondText("Invalid phone number", status = HttpStatusCode.BadRequest)
            else
                call.respond(UserDTO().apply {
                    id = UUID.randomUUID()
                    phoneNumber = number
                })
        }
    }
}