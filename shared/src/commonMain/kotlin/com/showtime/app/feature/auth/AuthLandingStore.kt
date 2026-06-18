package com.showtime.app.feature.auth

import com.showtime.app.core.mvi.MviStore

class AuthLandingStore : MviStore<AuthLandingState, AuthLandingIntent, AuthLandingMutation, AuthLandingEffect>(
    initialState = AuthLandingState,
    reducer = authLandingReducer
) {
    override fun onIntent(intent: AuthLandingIntent) {
        when (intent) {
            AuthLandingIntent.LoginClicked -> sendEffect(AuthLandingEffect.NavigateToLogin)
            AuthLandingIntent.SignupClicked -> sendEffect(AuthLandingEffect.NavigateToSignup)
        }
    }
}
