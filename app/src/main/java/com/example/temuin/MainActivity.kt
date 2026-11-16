package com.example.temuin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import coil.Coil
import coil.ImageLoader
import com.example.temuin.data.local.TokenStore
import com.example.temuin.data.remote.ApiClient
import com.example.temuin.navigation.AppNavigation
import com.example.temuin.ui.theme.TemuinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenStore.init(this)
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .okHttpClient { ApiClient.imageHttpClient }
                .crossfade(true)
                .build()
        )
        setContent {
            TemuinTheme {
                AppNavigation()
            }
        }
    }
}