package com.example.temuin.data.remote

import com.example.temuin.data.local.TokenStore
import com.google.gson.Gson
import okhttp3.HttpUrl.Companion.toHttpUrl
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
    /**
     * OkHttp untuk Coil (gambar): kirim header ngrok & ganti URL "localhost"/"10.0.2.2"
     * dari backend ke host BASE_URL supaya bisa dibuka dari HP/emulator mana pun.
     */
    val imageHttpClient: OkHttpClient by lazy {
        val base = ApiConfig.BASE_URL.toHttpUrl()
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                var url = chain.request().url
                if (url.host in setOf("localhost", "127.0.0.1", "10.0.2.2") && url.host != base.host) {
                    url = url.newBuilder().scheme(base.scheme).host(base.host).port(base.port).build()
                }
                chain.proceed(
                    chain.request().newBuilder()
                        .url(url)
                        .header("ngrok-skip-browser-warning", "1")
                        .build()
                )
            }
            .build()
    }

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
