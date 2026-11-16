package com.example.temuin.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home?locationFilter={locationFilter}") {
        const val BASE_ROUTE = "home"
        const val ARG_LOCATION_FILTER = "locationFilter"
        fun createRoute(locationFilter: String? = null): String {
            return if (!locationFilter.isNullOrBlank()) {
                "home?locationFilter=${Uri.encode(locationFilter)}"
            } else {
                "home"
            }
        }
    }
    object Peta : Screen("peta")
    object Lapor : Screen("lapor")
    object Inbox : Screen("inbox")
    object Profil : Screen("profil")
    object EditProfile : Screen("edit_profile")
    object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: String): String = "item_detail/$itemId"
    }
}