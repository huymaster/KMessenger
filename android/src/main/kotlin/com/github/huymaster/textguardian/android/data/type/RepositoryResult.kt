package com.github.huymaster.textguardian.android.data.type

sealed class RepositoryResult<out T>(open val message: String? = null) {
    companion object {
        val EMPTY = Empty()
    }

    data class Success<T>(
        val data: T? = null,
        override val message: String? = null
    ) : RepositoryResult<T>(message)

    data class Error(
        val throwable: Throwable? = null,
        override val message: String? = null
    ) : RepositoryResult<Nothing>(message)

    data class Other<T>(
        val data: T? = null,
        override val message: String? = null
    ) : RepositoryResult<T>(message)

    class Empty() : RepositoryResult<Nothing>()
}

