package com.finorix.signals.presentation.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finorix.signals.presentation.screens.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : Screen("splash", "SPLASH", Icons.Default.Home)
    object Welcome : Screen("welcome", "WELCOME", Icons.Default.Home)
    object Login : Screen("login", "LOGIN", Icons.Default.Home)
    object Signup : Screen("signup", "SIGNUP", Icons.Default.Home)
    object ForgotPassword : Screen("forgot_password", "FORGOT_PASSWORD", Icons.Default.Home)
    object Home : Screen("home", "HOME", Icons.Default.Home)
    object Signal : Screen("signal", "ACTIVE FINORIX SOFTWARE", Icons.Default.Settings)
    object TrackOrder : Screen("track_order", "TRACK ORDER", Icons.Default.Search)
    object Settings : Screen("settings", "SETTINGS", Icons.Default.Settings)
    object Profile : Screen("profile", "PROFILE", Icons.Default.Person)
}

val topNavItems = listOf(
    Screen.Signal,
    Screen.TrackOrder,
    Screen.Settings,
    Screen.Profile
)

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        // Top Pill-style Navigation Bar
    val authViewModel: com.finorix.signals.presentation.screens.auth.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    val scrollState = rememberScrollState()
    val authRoutes = listOf(Screen.Splash.route, Screen.Welcome.route, Screen.Login.route, Screen.Signup.route, Screen.ForgotPassword.route)
    val showTopBar = !authRoutes.contains(currentRoute)

        if (showTopBar) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    
                    if (isSelected) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = PureBlack
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            NavButtonContent(screen, isSelected)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NeonGreen
                            ),
                            border = BorderStroke(1.dp, NeonGreen),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            NavButtonContent(screen, isSelected)
                        }
                    }
                }
            }
        }

        // Main Content Area wrapped in Glowing Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(if (showTopBar) Modifier.neonBorder() else Modifier),
            shape = RoundedCornerShape(if (showTopBar) 20.dp else 0.dp),
            color = if (showTopBar) CardBackground else PureBlack
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Splash.route) { 
                    SplashScreen { 
                        val startRoute = if (currentUser != null) Screen.Home.route else Screen.Welcome.route
                        navController.navigate(startRoute) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } 
                }
                composable(Screen.Welcome.route) {
                    com.finorix.signals.presentation.screens.auth.WelcomeScreen(
                        onLoginClick = { navController.navigate(Screen.Login.route) },
                        onSignupClick = { navController.navigate(Screen.Signup.route) }
                    )
                }
                composable(Screen.Login.route) {
                    com.finorix.signals.presentation.screens.auth.LoginScreen(
                        onSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        },
                        onSignupClick = { navController.navigate(Screen.Signup.route) },
                        onForgotClick = { navController.navigate(Screen.ForgotPassword.route) }
                    )
                }
                composable(Screen.Signup.route) {
                    com.finorix.signals.presentation.screens.auth.SignupScreen(
                        onSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        },
                        onLoginClick = { navController.navigate(Screen.Login.route) }
                    )
                }
                composable(Screen.ForgotPassword.route) {
                    com.finorix.signals.presentation.screens.auth.ForgotPasswordScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    Screen.Home.route,
                    enterTransition = { fadeIn(animationSpec = tween(500)) + slideInHorizontally() },
                    exitTransition = { fadeOut(animationSpec = tween(500)) + slideOutHorizontally() }
                ) { HomeScreen() }
                composable(
                    Screen.Signal.route,
                    enterTransition = { fadeIn(animationSpec = tween(500)) + slideInHorizontally() },
                    exitTransition = { fadeOut(animationSpec = tween(500)) + slideOutHorizontally() }
                ) { SignalScreen() }
                composable(
                    Screen.TrackOrder.route,
                    enterTransition = { fadeIn(animationSpec = tween(500)) + slideInHorizontally() },
                    exitTransition = { fadeOut(animationSpec = tween(500)) + slideOutHorizontally() }
                ) { TrackOrderScreen() }
                composable(
                    Screen.Settings.route,
                    enterTransition = { fadeIn(animationSpec = tween(500)) + slideInHorizontally() },
                    exitTransition = { fadeOut(animationSpec = tween(500)) + slideOutHorizontally() }
                ) { 
                    SettingsScreen(
                        onSignOut = {
                            authViewModel.signOut()
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable(
                    Screen.Profile.route,
                    enterTransition = { fadeIn(animationSpec = tween(500)) + slideInHorizontally() },
                    exitTransition = { fadeOut(animationSpec = tween(500)) + slideOutHorizontally() }
                ) { com.finorix.signals.presentation.screens.profile.ProfileScreen() }
            }
        }
    }
}

@Composable
private fun NavButtonContent(screen: Screen, isSelected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.title,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = screen.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}
