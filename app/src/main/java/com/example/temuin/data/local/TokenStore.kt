package com.example.temuin.data.local

import android.content.Context

/**
 * Akses global ke token login untuk interceptor [com.example.temuin.data.remote.ApiClient].
 * Menyimpan lewat [TokenManager] (dipakai juga oleh login/sign up), jadi token-nya sama.
 * Panggil [init] sekali di MainActivity.
 */
object TokenStore {
    private var manager: TokenManager? = null

    fun init(context: Context) {
        if (manager == null) manager = TokenManager(context.applicationContext)
    }

    val token: String? get() = manager?.getToken()

    fun clear() = manager?.clearToken()
}
