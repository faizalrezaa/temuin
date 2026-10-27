package com.example.temuin.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.temuin.ui.auth.LoginScreen
import com.example.temuin.ui.auth.SignupScreen
import com.example.temuin.ui.auth.AuthViewModel
import com.example.temuin.ui.auth.SubmissionScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onSignupSuccess = {
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            SubmissionScreen(
                viewModel = authViewModel,
                onBackToForm = { navController.popBackStack() }
            )
        }
    }
}
