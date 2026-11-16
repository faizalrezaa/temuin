package com.example.temuin.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.temuin.data.source.fake.FakeItemRepository
import com.example.temuin.data.source.fake.FakeLaporanRepository
import com.example.temuin.ui.auth.LoginScreen
import com.example.temuin.ui.auth.SignupScreen
import com.example.temuin.ui.components.AppTopBar
import com.example.temuin.ui.components.BottomNavigationBar
import com.example.temuin.ui.home.HomeScreen
import com.example.temuin.ui.profile.EditProfileScreen
import com.example.temuin.ui.profile.ProfileScreen
import com.example.temuin.ui.home.HomeViewModel
import com.example.temuin.ui.report.LaporScreen
import com.example.temuin.ui.report.LaporViewModel

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Peta.route,
        Screen.Inbox.route,
        Screen.Profil.route
    )

    val topBarRoutes = listOf(
        Screen.Home.route,
        Screen.Peta.route,
        Screen.Lapor.route,
        Screen.Inbox.route
    )

    Scaffold(
        topBar = {
            if (currentRoute in topBarRoutes) {
                AppTopBar(
                    showBackButton = currentRoute == Screen.Lapor.route,
                    onBackClick = { navController.navigateUp() },
                    onProfileClick = {
                        if (currentRoute != Screen.Profil.route) {
                            navController.navigate(Screen.Profil.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToSignup = {
                        navController.navigate(Screen.Signup.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Signup.route) {
                SignupScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    },
                    onSignupSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val viewModel: HomeViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return HomeViewModel(FakeItemRepository()) as T
                        }
                    }
                )
                HomeScreen(viewModel = viewModel)
            }

            composable(Screen.Peta.route) {
                PlaceholderScreen("Peta")
            }

            composable(Screen.Lapor.route) {
                val viewModel: LaporViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return LaporViewModel(FakeLaporanRepository()) as T
                        }
                    }
                )
                LaporScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Inbox.route) {
                PlaceholderScreen("Inbox")
            }

            composable(Screen.Profil.route) {
                ProfileScreen(
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Halaman $name (Segera Hadir)")
    }
}