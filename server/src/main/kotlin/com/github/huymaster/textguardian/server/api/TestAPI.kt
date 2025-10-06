package com.github.huymaster.textguardian.server.api

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

object TestAPI : BaseAPI(0) {
    override fun Route.register() {
        post("/testUploadAttachment") {
            val multipart = call.receiveMultipart()
            multipart.forEachPart {
                println(it.headers.toMap())
            }
            call.respondText("OK", status = HttpStatusCode.OK)
        }
    }
}