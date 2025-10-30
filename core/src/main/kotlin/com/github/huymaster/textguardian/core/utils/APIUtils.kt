package com.github.huymaster.textguardian.core.utils

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

private class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = originalRequest.header("Authorization")
        if (token != null && !token.startsWith("Bearer ", true)) {
            val request = originalRequest.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $token")
            return chain.proceed(request.build())
        }
        return chain.proceed(originalRequest)
    }
}

const val API_BASE_URL = "https://api-textguardian.ddns.net"
private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .addInterceptor(AuthInterceptor())
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