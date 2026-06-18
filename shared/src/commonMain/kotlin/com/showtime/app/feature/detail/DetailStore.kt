package com.showtime.app.feature.detail

import com.showtime.app.core.mvi.MviStore
import com.showtime.app.core.result.AppResult
import com.showtime.app.data.repository.LibraryRepository
import com.showtime.app.data.repository.MovieDetailRepository

class DetailStore(
    private val detailRepo: MovieDetailRepository,
    private val libraryRepo: LibraryRepository,
    private val movieId: String
) : MviStore<DetailState, DetailIntent, DetailMutation, DetailEffect>(
    initialState = DetailState(),
    reducer = detailReducer
) {
    init {
        // network → Room
        launch {
            runCatching { detailRepo.refresh(movieId) }
                .onFailure { mutate(DetailMutation.RefreshFailed(it.message ?: "Failed to load details")) }
        }
        // observe Room (SSOT) — also re-emits when a toggle flips a flag
        launch {
            detailRepo.observeDetail(movieId).collect { detail ->
                mutate(DetailMutation.Loaded(detail))
            }
        }
    }

    override fun onIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.ToggleFavorite -> launch {
                val res = libraryRepo.toggleFavorite(movieId, intent.current)
                if (res is AppResult.Error) sendEffect(DetailEffect.ShowMessage(res.message))
            }
            is DetailIntent.ToggleWatchlist -> launch {
                val res = libraryRepo.toggleWatchlist(movieId, intent.current)
                if (res is AppResult.Error) sendEffect(DetailEffect.ShowMessage(res.message))
            }
        }
    }
}
