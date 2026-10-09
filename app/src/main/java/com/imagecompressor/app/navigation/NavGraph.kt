package com.imagecompressor.app.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object Account : Screen("account")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")
}
