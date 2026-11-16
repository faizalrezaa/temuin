package com.example.temuin.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Peta : Screen("peta")
    object Lapor : Screen("lapor")
    object Inbox : Screen("inbox")
    object Profil : Screen("profil")
    object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: String): String = "item_detail/$itemId"
    }
}