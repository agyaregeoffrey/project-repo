package com.activeminds.projectrepo.models.api

import com.dev.gka.plagiarismchecker.models.PlagiarismRequestBody
import com.dev.gka.plagiarismchecker.models.PlagiarismResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://plagiarism-checker-and-auto-citation-generator-multi-lingual.p.rapidapi.com/"

private val logger = HttpLoggingInterceptor()
    .setLevel(HttpLoggingInterceptor.Level.BODY)

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val credentials: Interceptor = Interceptor { chain ->
    val original = chain.request()
    val request = original.newBuilder()
        .header("content-type", "application/json")
        .header("X-RapidAPI-Key", "41030eedb5msh2dfb5e85ce80e08p160879jsn8891783c787a")
        .header("X-RapidAPI-Host", "plagiarism-checker-and-auto-citation-generator-multi-lingual.p.rapidapi.com")
        .method(original.method, original.body)
        .build()

    return@Interceptor chain.proceed(request)
}

private val client = OkHttpClient().newBuilder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .writeTimeout(20, TimeUnit.SECONDS)
    .addInterceptor(logger)
    .addInterceptor(credentials)
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .baseUrl(BASE_URL)
    .build()


interface ApiService {

    @POST("plagiarism/")
    fun plagiarismWithCall(@Body requestBody: PlagiarismRequestBody): Call<PlagiarismResponse>
}

object ApiClient {
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}