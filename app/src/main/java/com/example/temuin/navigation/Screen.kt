package com.example.temuin.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home") // Dummy route untuk tujuan setelah login/signup
}