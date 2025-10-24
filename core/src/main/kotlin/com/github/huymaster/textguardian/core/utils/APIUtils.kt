package com.github.huymaster.textguardian.core.utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

const val API_BASE_URL = "https://api-textguardian.ddns.net"
private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .build()

fun <T : Any> createService(clazz: KClass<T>, baseUrl: String = API_BASE_URL): T = createService(clazz.java, baseUrl)

fun <T : Any> createService(clazz: Class<T>, baseUrl: String = API_BASE_URL): T {
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(initJsonMapper()))
        .client(client)
        .build()
    return retrofit.create(clazz)
}