package com.github.huymaster

import com.github.huymaster.textguardian.server.module
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val token =
            "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJLTWVzc2VuZ2VyIiwiaXNzIjoiS01lc3NlbmdlciIsInVzZXJJZCI6IjM5MTE5MWZjLTFjYTktNDYxMC04Mjg4LTZmOGNjYmRkMjRhYSIsImV4cCI6MTc2MDcwNTY2NX0.qMq-OAYHUIIgMQiZiEAYILaBH9h-0_jFm9q3iT3IvASTBhRaEjmdU8xJlI35_0vKF8e-WzNmFy7M_B5Ywh-gfg"
        val id = "24dd9433-90e5-4bb0-8ede-bd4da654d773"
        client.get("/api/v1/conversation/$id/publicKeys") {
            headers[HttpHeaders.UserAgent] = "KMessenger-Debug"
            headers[HttpHeaders.Authorization] = "Bearer $token"
        }
    }
}