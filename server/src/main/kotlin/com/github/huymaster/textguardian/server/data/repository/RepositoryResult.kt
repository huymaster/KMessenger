package com.github.huymaster.textguardian.server.data.repository

import io.ktor.http.*

sealed class RepositoryResult(val message: String, val desiredStatus: HttpStatusCode = HttpStatusCode.BadRequest) {
    data class Success<T>(val data: T) : RepositoryResult("Success", HttpStatusCode.OK)
    class Error(message: String, desiredStatus: HttpStatusCode = HttpStatusCode.BadRequest) :
        RepositoryResult(message, desiredStatus)
}