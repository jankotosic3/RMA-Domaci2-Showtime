package com.showtime.app.feature.profile

import com.showtime.app.core.mvi.Effect
import com.showtime.app.core.mvi.Intent
import com.showtime.app.core.mvi.Mutation
import com.showtime.app.core.mvi.Reducer
import com.showtime.app.core.mvi.ViewState
import com.showtime.app.domain.model.UserProfile

data class ProfileState(
    val isLoading: Boolean = true,
    val user: UserProfile? = null,
    val bestScore: Float = 0f,
    val gamesPlayed: Int = 0,
    val favoritesCount: Int = 0,
    val watchlistCount: Int = 0,
    val error: String? = null
) : ViewState

sealed interface ProfileIntent : Intent {
    data object Load : ProfileIntent
    data object LogoutClicked : ProfileIntent
}

sealed interface ProfileMutation : Mutation {
    data object Loading : ProfileMutation
    data class Profile(val user: UserProfile) : ProfileMutation
    data class Stats(val best: Float, val plays: Int, val favs: Int, val watch: Int) : ProfileMutation
    data class Failed(val message: String) : ProfileMutation
}

sealed interface ProfileEffect : Effect {
    // App() observes AuthRepository.isLoggedIn and swaps to the auth graph automatically on logout,
    // so this is only used to surface load errors.
    data class ShowMessage(val message: String) : ProfileEffect
}

val profileReducer = Reducer<ProfileState, ProfileMutation> { state, mutation ->
    when (mutation) {
        ProfileMutation.Loading -> state.copy(isLoading = true, error = null)
        is ProfileMutation.Profile -> state.copy(isLoading = false, user = mutation.user)
        is ProfileMutation.Stats -> state.copy(
            isLoading = false,
            bestScore = mutation.best,
            gamesPlayed = mutation.plays,
            favoritesCount = mutation.favs,
            watchlistCount = mutation.watch
        )
        is ProfileMutation.Failed -> state.copy(isLoading = false, error = mutation.message)
    }
}
