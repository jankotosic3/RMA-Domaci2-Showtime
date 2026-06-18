package com.showtime.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onAuthenticated: () -> Unit,
    store: LoginStore = koinViewModel()
) {
    val state by store.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        store.effects.collect { effect ->
            when (effect) {
                LoginEffect.NavigateToMain -> onAuthenticated()
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome back", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = state.username,
                onValueChange = { store.onIntent(LoginIntent.UsernameChanged(it)) },
                label = { Text("Username") },
                singleLine = true,
                isError = state.usernameError != null,
                supportingText = state.usernameError?.let { { Text(it) } },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = { store.onIntent(LoginIntent.PasswordChanged(it)) },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = state.passwordError != null,
                supportingText = state.passwordError?.let { { Text(it) } },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            state.formError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Button(
                onClick = { store.onIntent(LoginIntent.Submit) },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Log in")
                }
            }

            TextButton(
                onClick = onBack,
                enabled = !state.isSubmitting,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            ) { Text("Back") }
        }
    }
}
