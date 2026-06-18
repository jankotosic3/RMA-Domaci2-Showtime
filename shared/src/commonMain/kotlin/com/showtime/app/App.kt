package com.showtime.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.showtime.app.data.repository.AuthRepository
import com.showtime.app.navigation.AuthNavGraph
import com.showtime.app.navigation.MainScaffold
import org.koin.compose.koinInject

@Composable
fun App() {
    // Configure Coil's singleton loader with the Ktor network fetcher so posters load
    // on BOTH Android and Desktop (Coil 3 has no built-in network fetcher on JVM).
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    MaterialTheme {
        val authRepo: AuthRepository = koinInject()
        val loggedIn by authRepo.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)

        when (loggedIn) {
            null -> SplashView()
            false -> {
                val navController = rememberNavController()
                AuthNavGraph(navController)
            }
            true -> MainScaffold()
        }
    }
}

@Composable
private fun SplashView() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
