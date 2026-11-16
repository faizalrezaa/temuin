package com.example.temuin.data.remote

import com.example.temuin.data.local.TokenStore
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiConfig {
    // Ganti ke "http://10.0.2.2:8080/api/v1/" kalau backend jalan lokal & pakai emulator.
    const val BASE_URL = "https://remissly-crumbier-michelle.ngrok-free.dev/api/v1/"
}

object ApiClient {
    private val gson = Gson()

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .header("ngrok-skip-browser-warning", "1")
                    .header("Accept", "application/json")
                TokenStore.token?.let { builder.header("Authorization", "Bearer $it") }
                val response = chain.proceed(builder.build())
                // Token kadaluarsa / invalid → hapus sesi
                if (response.code == 401 && TokenStore.token != null &&
                    !chain.request().url.encodedPath.contains("/auth/")
                ) {
                    TokenStore.clear()
                }
                response
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: TemuinApi by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TemuinApi::class.java)
    }

    /** Ubah exception jadi pesan yang bisa ditampilkan ke user. */
    fun errorMessage(e: Throwable): String = when (e) {
        is HttpException -> {
            val body = runCatching {
                gson.fromJson(e.response()?.errorBody()?.string(), ApiErrorEnvelope::class.java)
            }.getOrNull()?.error
            val detail = body?.details?.values?.firstOrNull()
            when {
                detail != null -> "${body.message ?: "Input tidak valid"}: $detail"
                body?.message != null -> body.message
                else -> "Terjadi kesalahan (${e.code()})"
            }
        }
        is IOException -> "Tidak dapat terhubung ke server. Periksa koneksi internet."
        else -> e.message ?: "Terjadi kesalahan"
    }
}
