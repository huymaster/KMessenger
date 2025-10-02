package com.github.huymaster.textguardian.core.api

import retrofit2.Response
import retrofit2.http.GET

interface APIBase {
    @GET("/health")
    suspend fun health(): Response<String>
}