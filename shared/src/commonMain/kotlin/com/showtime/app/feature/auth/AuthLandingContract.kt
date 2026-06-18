package com.showtime.app.feature.auth

import com.showtime.app.core.mvi.Effect
import com.showtime.app.core.mvi.Intent
import com.showtime.app.core.mvi.Mutation
import com.showtime.app.core.mvi.Reducer
import com.showtime.app.core.mvi.ViewState

data object AuthLandingState : ViewState

sealed interface AuthLandingIntent : Intent {
    data object LoginClicked : AuthLandingIntent
    data object SignupClicked : AuthLandingIntent
}

// Landing has no state transitions; the marker keeps the MVI quad uniform.
sealed interface AuthLandingMutation : Mutation

sealed interface AuthLandingEffect : Effect {
    data object NavigateToLogin : AuthLandingEffect
    data object NavigateToSignup : AuthLandingEffect
}

val authLandingReducer = Reducer<AuthLandingState, AuthLandingMutation> { state, _ -> state }
