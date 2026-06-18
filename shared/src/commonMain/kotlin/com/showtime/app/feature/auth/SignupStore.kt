package com.showtime.app.feature.auth

import com.showtime.app.core.mvi.MviStore
import com.showtime.app.core.result.AppResult
import com.showtime.app.data.repository.AuthRepository

class SignupStore(
    private val authRepo: AuthRepository
) : MviStore<SignupState, SignupIntent, SignupMutation, SignupEffect>(
    initialState = SignupState(),
    reducer = signupReducer
) {
    private val usernameRegex = Regex("^[A-Za-z0-9_]{3,}$")

    override fun onIntent(intent: SignupIntent) {
        when (intent) {
            is SignupIntent.FullNameChanged -> mutate(SignupMutation.FullNameChanged(intent.v))
            is SignupIntent.UsernameChanged -> mutate(SignupMutation.UsernameChanged(intent.v.trim()))
            is SignupIntent.PasswordChanged -> mutate(SignupMutation.PasswordChanged(intent.v))
            SignupIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val s = state.value
        val fullNameError = if (s.fullName.isBlank()) "Enter your full name" else null
        val usernameError = when {
            s.username.isBlank() -> "Enter a username"
            !usernameRegex.matches(s.username) -> "3+ letters, numbers, or underscores"
            else -> null
        }
        val passwordError = if (s.password.length < 8) "At least 8 characters" else null

        if (fullNameError != null || usernameError != null || passwordError != null) {
            mutate(SignupMutation.Validation(fullNameError, usernameError, passwordError))
            return
        }

        launch {
            mutate(SignupMutation.Submitting)
            when (val res = authRepo.signup(s.fullName.trim(), s.username, s.password)) {
                is AppResult.Success -> sendEffect(SignupEffect.NavigateToMain)
                is AppResult.Error ->
                    if (res.message == "Username already taken") {
                        mutate(SignupMutation.UsernameTaken(res.message))
                    } else {
                        mutate(SignupMutation.FormError(res.message))
                    }
            }
        }
    }
}
