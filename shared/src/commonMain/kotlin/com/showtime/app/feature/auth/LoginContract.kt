package com.showtime.app.feature.auth

import com.showtime.app.core.mvi.Effect
import com.showtime.app.core.mvi.Intent
import com.showtime.app.core.mvi.Mutation
import com.showtime.app.core.mvi.Reducer
import com.showtime.app.core.mvi.ViewState

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null
) : ViewState

sealed interface LoginIntent : Intent {
    data class UsernameChanged(val v: String) : LoginIntent
    data class PasswordChanged(val v: String) : LoginIntent
    data object Submit : LoginIntent
}

sealed interface LoginMutation : Mutation {
    data class UsernameChanged(val v: String) : LoginMutation
    data class PasswordChanged(val v: String) : LoginMutation
    data class Validation(val usernameError: String?, val passwordError: String?) : LoginMutation
    data object Submitting : LoginMutation
    data class FormError(val message: String) : LoginMutation
}

sealed interface LoginEffect : Effect {
    data object NavigateToMain : LoginEffect
}

val loginReducer = Reducer<LoginState, LoginMutation> { state, mutation ->
    when (mutation) {
        is LoginMutation.UsernameChanged ->
            state.copy(username = mutation.v, usernameError = null, formError = null)
        is LoginMutation.PasswordChanged ->
            state.copy(password = mutation.v, passwordError = null, formError = null)
        is LoginMutation.Validation ->
            state.copy(usernameError = mutation.usernameError, passwordError = mutation.passwordError)
        LoginMutation.Submitting ->
            state.copy(isSubmitting = true, usernameError = null, passwordError = null, formError = null)
        is LoginMutation.FormError ->
            state.copy(isSubmitting = false, formError = mutation.message)
    }
}
