package com.showtime.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.showtime.app.feature.auth.AuthLandingScreen
import com.showtime.app.feature.auth.LoginScreen
import com.showtime.app.feature.auth.SignupScreen

// Unauthenticated flow: landing → login / signup.
// On successful login/signup the token is saved, AuthRepository.isLoggedIn flips,
// and App() swaps to the main graph automatically — so the screens don't navigate to main.
@Composable
fun AuthNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AuthLanding) {
        composable<AuthLanding> {
            AuthLandingScreen(
                onNavigateLogin = { navController.navigate(Login) },
                onNavigateSignup = { navController.navigate(Signup) }
            )
        }
        composable<Login> {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onAuthenticated = { /* App swaps on isLoggedIn */ }
            )
        }
        composable<Signup> {
            SignupScreen(
                onBack = { navController.popBackStack() },
                onAuthenticated = { /* App swaps on isLoggedIn */ }
            )
        }
    }
}
