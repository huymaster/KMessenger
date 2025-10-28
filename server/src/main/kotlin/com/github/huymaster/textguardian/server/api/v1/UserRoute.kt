package com.github.huymaster.textguardian.server.api.v1

import com.github.huymaster.textguardian.core.api.type.UserInfo
import com.github.huymaster.textguardian.core.dto.UserDTO
import com.github.huymaster.textguardian.core.entity.UserEntity
import com.github.huymaster.textguardian.server.api.APIVersion1.sendErrorResponse
import com.github.huymaster.textguardian.server.api.SubRoute
import com.github.huymaster.textguardian.server.data.repository.RepositoryResult
import com.github.huymaster.textguardian.server.data.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.core.component.inject

object UserRoute : SubRoute() {
    suspend fun getMe(call: ApplicationCall) {
        val user: UserRepository by inject()
        val principal = call.principal<JWTPrincipal>()
        val payload = principal?.payload?.getClaim(UserDTO.ID_FIELD)?.asString()
        val rr = user.getUserByUserId(payload)
        if ((payload == null) || (rr is RepositoryResult.Error) || ((rr as RepositoryResult.Success<*>).data !is UserEntity)) {
            call.sendErrorResponse("Not allowed to retrieve user info", status = HttpStatusCode.Unauthorized)
            return
        }
        call.respond(UserInfo(rr.data as UserEntity))
    }

    suspend fun updateInfo(call: ApplicationCall) {

    }
}