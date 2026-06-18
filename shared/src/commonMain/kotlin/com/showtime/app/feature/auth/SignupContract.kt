package com.showtime.app.feature.auth

import com.showtime.app.core.mvi.Effect
import com.showtime.app.core.mvi.Intent
import com.showtime.app.core.mvi.Mutation
import com.showtime.app.core.mvi.Reducer
import com.showtime.app.core.mvi.ViewState

data class SignupState(
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val fullNameError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null
) : ViewState

sealed interface SignupIntent : Intent {
    data class FullNameChanged(val v: String) : SignupIntent
    data class UsernameChanged(val v: String) : SignupIntent
    data class PasswordChanged(val v: String) : SignupIntent
    data object Submit : SignupIntent
}

sealed interface SignupMutation : Mutation {
    data class FullNameChanged(val v: String) : SignupMutation
    data class UsernameChanged(val v: String) : SignupMutation
    data class PasswordChanged(val v: String) : SignupMutation
    data class Validation(
        val fullNameError: String?,
        val usernameError: String?,
        val passwordError: String?
    ) : SignupMutation
    data object Submitting : SignupMutation
    data class UsernameTaken(val message: String) : SignupMutation
    data class FormError(val message: String) : SignupMutation
}

sealed interface SignupEffect : Effect {
    data object NavigateToMain : SignupEffect
}

val signupReducer = Reducer<SignupState, SignupMutation> { state, mutation ->
    when (mutation) {
        is SignupMutation.FullNameChanged ->
            state.copy(fullName = mutation.v, fullNameError = null, formError = null)
        is SignupMutation.UsernameChanged ->
            state.copy(username = mutation.v, usernameError = null, formError = null)
        is SignupMutation.PasswordChanged ->
            state.copy(password = mutation.v, passwordError = null, formError = null)
        is SignupMutation.Validation -> state.copy(
            fullNameError = mutation.fullNameError,
            usernameError = mutation.usernameError,
            passwordError = mutation.passwordError
        )
        SignupMutation.Submitting -> state.copy(
            isSubmitting = true,
            fullNameError = null, usernameError = null, passwordError = null, formError = null
        )
        is SignupMutation.UsernameTaken ->
            state.copy(isSubmitting = false, usernameError = mutation.message)
        is SignupMutation.FormError ->
            state.copy(isSubmitting = false, formError = mutation.message)
    }
}
