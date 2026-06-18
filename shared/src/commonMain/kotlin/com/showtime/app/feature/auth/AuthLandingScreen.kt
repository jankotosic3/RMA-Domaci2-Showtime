package com.showtime.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthLandingScreen(
    onNavigateLogin: () -> Unit,
    onNavigateSignup: () -> Unit,
    store: AuthLandingStore = koinViewModel()
) {
    LaunchedEffect(Unit) {
        store.effects.collect { effect ->
            when (effect) {
                AuthLandingEffect.NavigateToLogin -> onNavigateLogin()
                AuthLandingEffect.NavigateToSignup -> onNavigateSignup()
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Showtime", style = MaterialTheme.typography.displaySmall)
            Text(
                "Discover movies, build your library, and play the quiz.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )
            Button(
                onClick = { store.onIntent(AuthLandingIntent.LoginClicked) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Log in") }
            OutlinedButton(
                onClick = { store.onIntent(AuthLandingIntent.SignupClicked) },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) { Text("Create account") }
        }
    }
}
