package com.github.huymaster.textguardian.server.data.repository

import io.ktor.http.*

sealed class RepositoryResult(
    open val message: String,
    open val desiredStatus: HttpStatusCode = HttpStatusCode.BadRequest
) {
    data class Success<T>(
        val data: T,
        override val message: String = "Success",
        override val desiredStatus: HttpStatusCode = HttpStatusCode.OK
    ) : RepositoryResult(message, desiredStatus)

    data class Error(
        override val message: String,
        override val desiredStatus: HttpStatusCode = HttpStatusCode.BadRequest
    ) : RepositoryResult(message, desiredStatus)
}