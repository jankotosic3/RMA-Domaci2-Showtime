package com.showtime.app.feature.auth

import com.showtime.app.core.mvi.MviStore
import com.showtime.app.core.result.AppResult
import com.showtime.app.data.repository.AuthRepository

class LoginStore(
    private val authRepo: AuthRepository
) : MviStore<LoginState, LoginIntent, LoginMutation, LoginEffect>(
    initialState = LoginState(),
    reducer = loginReducer
) {
    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UsernameChanged -> mutate(LoginMutation.UsernameChanged(intent.v.trim()))
            is LoginIntent.PasswordChanged -> mutate(LoginMutation.PasswordChanged(intent.v))
            LoginIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val s = state.value
        val usernameError = if (s.username.isBlank()) "Enter your username" else null
        val passwordError = if (s.password.isBlank()) "Enter your password" else null
        if (usernameError != null || passwordError != null) {
            mutate(LoginMutation.Validation(usernameError, passwordError))
            return
        }

        launch {
            mutate(LoginMutation.Submitting)
            when (val res = authRepo.login(s.username, s.password)) {
                is AppResult.Success -> sendEffect(LoginEffect.NavigateToMain)
                is AppResult.Error -> mutate(LoginMutation.FormError(res.message))
            }
        }
    }
}
