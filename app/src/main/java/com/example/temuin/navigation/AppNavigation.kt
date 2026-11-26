package com.example.temuin.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.temuin.data.local.TokenManager
import com.example.temuin.data.remote.NetworkModule
import androidx.lifecycle.createSavedStateHandle
import com.example.temuin.data.local.TokenStore
import com.example.temuin.data.repository.ProfileRepository
import com.example.temuin.data.source.remote.RemoteAuthRepository
import com.example.temuin.data.source.remote.RemoteInboxRepository
import com.example.temuin.data.source.remote.RemoteItemRepository
import com.example.temuin.data.source.remote.RemoteLaporanRepository
import com.example.temuin.data.source.remote.RemotePickupPointRepository
import com.example.temuin.ui.detail.ItemDetailScreen
import com.example.temuin.ui.detail.ItemDetailViewModel
import com.example.temuin.ui.auth.AuthViewModel
import com.example.temuin.ui.auth.LoginScreen
import com.example.temuin.ui.auth.SignupScreen
import com.example.temuin.ui.components.AppTopBar
import com.example.temuin.ui.help.HelpScreen
import com.example.temuin.ui.help.HelpViewModel
import com.example.temuin.ui.components.BottomNavigationBar
import com.example.temuin.ui.home.HomeScreen
import com.example.temuin.ui.profile.EditProfileScreen
import com.example.temuin.ui.profile.ProfileScreen
import com.example.temuin.ui.home.HomeViewModel
import com.example.temuin.ui.inbox.InboxScreen
import com.example.temuin.ui.inbox.InboxViewModel
import com.example.temuin.ui.match.MatchRecommendationScreen
import com.example.temuin.ui.match.MatchViewModel
import com.example.temuin.ui.peta.PetaScreen
import com.example.temuin.ui.peta.PetaViewModel
import com.example.temuin.ui.report.LaporScreen
import com.example.temuin.ui.report.LaporViewModel
import com.example.temuin.ui.theme.OrangePrimary

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentUser by ProfileRepository.currentUser.collectAsState()
    val profileRepository = remember { ProfileRepository() }

    LaunchedEffect(currentRoute) {
        if (TokenStore.token != null) {
            profileRepository.getMe()
        }
    }

    // ── AuthViewModel dibuat sekali dan dishare antara Login & Signup ──────────
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val tokenManager = TokenManager(context)
                val repository = RemoteAuthRepository(NetworkModule.authApiService, tokenManager)
                return AuthViewModel(repository) as T
            }
        }
    )

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Peta.route,
        Screen.Inbox.route,
        Screen.Profil.route
    )

    val topBarRoutes = listOf(
        Screen.Home.route,
        Screen.Lapor.route,
        Screen.Inbox.route
    )

    Scaffold(
        topBar = {
            if (currentRoute in topBarRoutes) {
                AppTopBar(
                    avatarUrl = currentUser?.avatarUrl,
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
        floatingActionButton = {
            if (currentRoute == Screen.Home.route) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Help.route) },
                    containerColor = OrangePrimary,
                    contentColor = Color.White,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SmartToy,
                        contentDescription = "Bantuan AI Temuin",
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = remember { if (TokenStore.token != null) Screen.Home.route else Screen.Login.route },
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToSignup = {
                        navController.navigate(Screen.Signup.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.createRoute()) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
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
                        navController.navigate(Screen.Home.createRoute()) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }

            composable(
                route = Screen.Home.route,
                arguments = listOf(
                    navArgument(Screen.Home.ARG_LOCATION_FILTER) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val locationFilter = backStackEntry.arguments?.getString(Screen.Home.ARG_LOCATION_FILTER)
                val viewModel: HomeViewModel = viewModel(
                    key = locationFilter ?: "home",
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return HomeViewModel(RemoteItemRepository(), locationFilter) as T
                        }
                    }
                )
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { id -> navController.navigate(Screen.ItemDetail.createRoute(id)) },
                    onNavigateToMap = {
                        navController.navigate(Screen.Peta.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Peta.route) {
                val viewModel: PetaViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return PetaViewModel(
                                pickupPointRepository = RemotePickupPointRepository(),
                                itemRepository = RemoteItemRepository()
                            ) as T
                        }
                    }
                )
                PetaScreen(
                    viewModel = viewModel,
                    onOpenItem = { id -> navController.navigate(Screen.ItemDetail.createRoute(id)) },
                    onNavigateToHomeWithFilter = { filter ->
                        navController.navigate(Screen.Home.createRoute(filter)) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Lapor.route) {
                val viewModel: LaporViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return LaporViewModel(RemoteLaporanRepository(context)) as T
                        }
                    }
                )
                LaporScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.createRoute()) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Inbox.route) {
                val viewModel: InboxViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return InboxViewModel(RemoteInboxRepository()) as T
                        }
                    }
                )
                LaunchedEffect(Unit) { viewModel.refresh() }
                InboxScreen(viewModel = viewModel)
            }

            composable(Screen.Profil.route) {
                ProfileScreen(
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onOpenReport = { id -> navController.navigate(Screen.ItemDetail.createRoute(id)) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Help.route) {
                val helpViewModel: HelpViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return HelpViewModel(
                                profileNameProvider = {
                                    ProfileRepository.currentUser.value?.name
                                        ?: profileRepository.getMe().getOrNull()?.name
                                }
                            ) as T
                        }
                    }
                )
                HelpScreen(
                    viewModel = helpViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigate = { topic ->
                        val destination = when (topic) {
                            "REPORTING" -> Screen.Lapor.route
                            "SEARCH" -> Screen.Home.createRoute()
                            "MAP_CUSTODY" -> Screen.Peta.route
                            "CLAIM_INBOX" -> Screen.Inbox.route
                            else -> null
                        }
                        if (destination != null) {
                            if (topic == "SEARCH" && navController.popBackStack(Screen.Home.route, false)) {
                                return@HelpScreen
                            }
                            navController.navigate(destination) { launchSingleTop = true }
                        }
                    },
                    onOpenReport = { id ->
                        navController.navigate(Screen.ItemDetail.createRoute(id))
                    },
                )
            }

            composable(
                route = Screen.ItemDetail.route,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) {
                val viewModel: ItemDetailViewModel = viewModel {
                    ItemDetailViewModel(createSavedStateHandle(), RemoteItemRepository())
                }
                ItemDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onFindMatches = {
                        navController.navigate(
                            Screen.MatchRecommendation.createRoute(viewModel.itemId)
                        )
                    }
                )
            }

            composable(
                route = Screen.MatchRecommendation.route,
                arguments = listOf(navArgument("reportId") { type = NavType.StringType })
            ) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId").orEmpty()
                val matchViewModel: MatchViewModel = viewModel(key = "matches:$reportId")

                LaunchedEffect(reportId) {
                    matchViewModel.loadMatches(reportId)
                }

                val uiState by matchViewModel.uiState.collectAsState()

                MatchRecommendationScreen(
                    uiState = uiState,
                    onRetry = matchViewModel::retry,
                    onManualSearch = {
                        navController.navigate(Screen.Home.createRoute()) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onOpenReport = { id ->
                        navController.navigate(Screen.ItemDetail.createRoute(id))
                    },
                    onLogin = {
                        TokenStore.clear()
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
