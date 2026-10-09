package com.imagecompressor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.imagecompressor.app.navigation.Screen
import com.imagecompressor.app.ui.screens.AccountScreen
import com.imagecompressor.app.ui.screens.ForgotPasswordScreen
import com.imagecompressor.app.ui.screens.HistoryScreen
import com.imagecompressor.app.ui.screens.HomeScreen
import com.imagecompressor.app.ui.screens.LoginScreen
import com.imagecompressor.app.ui.screens.RegisterScreen
import com.imagecompressor.app.ui.screens.SettingsScreen
import com.imagecompressor.app.ui.theme.ImageCompressorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageCompressorTheme {
                ImageCompressorApp()
            }
        }
    }
}

private data class BottomDestination(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun ImageCompressorApp() {
    val navController = rememberNavController()
    val bottomDestinations = listOf(
        BottomDestination(Screen.Home, "Home", Icons.Filled.Home),
        BottomDestination(Screen.History, "History", Icons.Filled.History),
        BottomDestination(Screen.Account, "Account", Icons.Filled.Person),
        BottomDestination(Screen.Settings, "Settings", Icons.Filled.Settings)
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            NavigationBar {
                bottomDestinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any { it.route == destination.screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize().let { it },
            ) {
                composable(Screen.Home.route) { HomeScreen(paddingValues) }
                composable(Screen.History.route) { HistoryScreen(paddingValues) }
                composable(Screen.Settings.route) { SettingsScreen(paddingValues) }
                composable(Screen.Account.route) {
                    AccountScreen(
                        paddingValues = paddingValues,
                        onNavigateLogin = { navController.navigate(Screen.Login.route) },
                        onNavigateRegister = { navController.navigate(Screen.Register.route) }
                    )
                }
                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateRegister = { navController.navigate(Screen.Register.route) },
                        onNavigateForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                        onLoggedIn = { navController.popBackStack(Screen.Account.route, false) }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateLogin = { navController.navigate(Screen.Login.route) },
                        onRegistered = { navController.popBackStack(Screen.Account.route, false) }
                    )
                }
                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
                }
            }
        }
    }
}
