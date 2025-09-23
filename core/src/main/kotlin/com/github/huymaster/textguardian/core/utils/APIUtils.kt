package com.github.huymaster.textguardian.core.utils

import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import kotlin.reflect.KClass

const val API_BASE_URL = "https://api-textguardian.ddns.net"
fun <T : Any> createService(clazz: KClass<T>): T = createService(clazz.java)

fun <T : Any> createService(clazz: Class<T>): T {
    val retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addConverterFactory(JacksonConverterFactory.create(DEFAULT_OBJECT_MAPPER))
        .build()
    return retrofit.create(clazz)
}