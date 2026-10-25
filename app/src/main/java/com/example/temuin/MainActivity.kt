package com.example.temuin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.temuin.navigation.AppNavigation
import com.example.temuin.ui.theme.TemuinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemuinTheme {
                AppNavigation()
            }
        }
    }
}