package com.showtime.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.showtime.app.feature.catalog.CatalogScreen
import com.showtime.app.feature.detail.DetailScreen
import com.showtime.app.feature.library.FavoritesScreen
import com.showtime.app.feature.library.WatchlistScreen
import com.showtime.app.feature.profile.ProfileScreen
import com.showtime.app.feature.quiz.QuizResultScreen
import com.showtime.app.feature.quiz.QuizScreen
import kotlin.reflect.KClass

private data class Tab(val route: Any, val routeClass: KClass<*>, val label: String, val icon: String)

private val tabs = listOf(
    Tab(Catalog, Catalog::class, "Catalog", "🎞️"),
    Tab(Favorites, Favorites::class, "Favorites", "❤️"),
    Tab(Watchlist, Watchlist::class, "Watchlist", "🔖"),
    Tab(Quiz, Quiz::class, "Quiz", "🎯"),
    Tab(Profile, Profile::class, "Profile", "👤")
)

@Composable
fun MainScaffold(navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentDest = backStack?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentDest?.hierarchyHasRoute(tab.routeClass) == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(Catalog) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(tab.icon) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Catalog,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            composable<Catalog> {
                CatalogScreen(onMovieClick = { navController.navigate(Detail(it)) })
            }
            composable<Favorites> {
                FavoritesScreen(onMovieClick = { navController.navigate(Detail(it)) })
            }
            composable<Watchlist> {
                WatchlistScreen(onMovieClick = { navController.navigate(Detail(it)) })
            }
            composable<Quiz> {
                QuizScreen(
                    onFinished = { score, correct, wrong, used ->
                        // Replace the Quiz entry so Back from the result returns to Catalog.
                        navController.navigate(QuizResult(score, correct, wrong, used)) {
                            popUpTo(Quiz) { inclusive = true }
                        }
                    },
                    onAbandon = { navController.popBackStack() }
                )
            }
            composable<QuizResult> { entry ->
                val result = entry.toRoute<QuizResult>()
                QuizResultScreen(
                    score = result.score,
                    correct = result.correct,
                    wrong = result.wrong,
                    usedSeconds = result.usedSeconds,
                    onPlayAgain = {
                        // Fresh Quiz entry (new ViewModel) in place of the result.
                        navController.navigate(Quiz) { popUpTo(QuizResult) { inclusive = true } }
                    },
                    onDone = { navController.popBackStack() }
                )
            }
            composable<Profile> { ProfileScreen() }
            composable<Detail> { entry ->
                val detail = entry.toRoute<Detail>()
                DetailScreen(movieId = detail.movieId, onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun androidx.navigation.NavDestination.hierarchyHasRoute(routeClass: KClass<*>): Boolean =
    hierarchy.any { it.hasRoute(routeClass) }
