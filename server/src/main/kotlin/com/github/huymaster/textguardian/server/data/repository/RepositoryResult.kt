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
    ) : RepositoryResult(message, desiredStatus) {
        constructor(data: T, desiredStatus: HttpStatusCode = HttpStatusCode.OK) :
                this(data, "Success", desiredStatus)
    }

    data class Error(
        override val message: String,
        override val desiredStatus: HttpStatusCode = HttpStatusCode.BadRequest
    ) : RepositoryResult(message, desiredStatus)

    fun ifStatus(status: HttpStatusCode, block: (RepositoryResult) -> Unit): RepositoryResult {
        if (this.desiredStatus == status) block(this)
        return this
    }

    fun ifStatus(status: Int, block: (RepositoryResult) -> Unit): RepositoryResult {
        if (this.desiredStatus.value == status) block(this)
        return this
    }

    fun <O> ifStatus(status: HttpStatusCode, block: (RepositoryResult) -> O): O? =
        if (this.desiredStatus == status) block(this)
        else null

    fun <O> ifStatus(status: Int, block: (RepositoryResult) -> O): O? =
        if (this.desiredStatus.value == status) block(this)
        else null

    fun ifSuccess(block: (Success<*>) -> Unit): RepositoryResult {
        if (this is Success<*>) block(this)
        return this
    }

    fun <O> ifSuccess(block: (Success<*>) -> O): O? =
        if (this is Success<*>) block(this)
        else null

    fun ifError(block: (Error) -> Unit): RepositoryResult {
        if (this is Error) block(this)
        return this
    }

    fun <O> ifError(block: (Error) -> O): O? =
        if (this is Error) block(this)
        else null
}