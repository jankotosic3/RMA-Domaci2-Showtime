package com.showtime.app.feature.profile

import com.showtime.app.core.mvi.MviStore
import com.showtime.app.data.repository.AuthRepository
import com.showtime.app.data.repository.LibraryRepository
import com.showtime.app.data.repository.QuizRepository
import kotlinx.coroutines.flow.combine

class ProfileStore(
    private val authRepo: AuthRepository,
    private val libraryRepo: LibraryRepository,
    private val quizRepo: QuizRepository
) : MviStore<ProfileState, ProfileIntent, ProfileMutation, ProfileEffect>(
    initialState = ProfileState(),
    reducer = profileReducer
) {
    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Load -> load()
            ProfileIntent.LogoutClicked -> logout()
        }
    }

    private fun load() {
        mutate(ProfileMutation.Loading)
        // Server identity (GET /me).
        launch {
            runCatching { authRepo.getProfile() }
                .onSuccess { mutate(ProfileMutation.Profile(it)) }
                .onFailure { mutate(ProfileMutation.Failed(it.message ?: "Failed to load profile")) }
        }
        // Room-sourced stats (SSOT) — keep observing so counts stay live.
        launch {
            combine(
                quizRepo.observeStats(),
                libraryRepo.observeFavoritesCount(),
                libraryRepo.observeWatchlistCount()
            ) { stats, favs, watch ->
                ProfileMutation.Stats(stats.bestScore, stats.gamesPlayed, favs, watch)
            }.collect { mutate(it) }
        }
    }

    // Centralized logout: clears token + user-scoped Room data. isLoggedIn then flips and
    // App() swaps to the auth landing automatically — no manual navigation needed here.
    private fun logout() = launch { authRepo.logout() }
}
