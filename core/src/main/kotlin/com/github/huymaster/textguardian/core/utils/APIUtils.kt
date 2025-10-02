package com.github.huymaster.textguardian.core.utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

const val API_BASE_URL = "https://api-textguardian.ddns.net"
private val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request()
            .newBuilder()
            .header("User-Agent", "KMessenger")
            .build()
        return@addInterceptor chain.proceed(request)
    }
    .connectTimeout(10, TimeUnit.SECONDS)
    .build()

fun <T : Any> createService(clazz: KClass<T>): T = createService(clazz.java)

fun <T : Any> createService(clazz: Class<T>): T {
    val retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(initObjectMapper()))
        .client(client)
        .build()
    return retrofit.create(clazz)
}