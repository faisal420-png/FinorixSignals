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
import androidx.compose.material.icons.filled.Person
import com.finorix.signals.util.neonBorder
import com.finorix.signals.presentation.theme.*
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
