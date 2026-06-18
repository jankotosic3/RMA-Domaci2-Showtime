package com.showtime.app.feature.detail

import com.showtime.app.core.mvi.Effect
import com.showtime.app.core.mvi.Intent
import com.showtime.app.core.mvi.Mutation
import com.showtime.app.core.mvi.Reducer
import com.showtime.app.core.mvi.ViewState
import com.showtime.app.domain.model.MovieDetail

data class DetailState(
    val isLoading: Boolean = true,
    val detail: MovieDetail? = null,
    val error: String? = null
) : ViewState

sealed interface DetailIntent : Intent {
    data class ToggleFavorite(val current: Boolean) : DetailIntent
    data class ToggleWatchlist(val current: Boolean) : DetailIntent
}

sealed interface DetailMutation : Mutation {
    data class Loaded(val detail: MovieDetail?) : DetailMutation
    data class RefreshFailed(val message: String) : DetailMutation
}

sealed interface DetailEffect : Effect {
    data class ShowMessage(val message: String) : DetailEffect
}

val detailReducer = Reducer<DetailState, DetailMutation> { state, mutation ->
    when (mutation) {
        is DetailMutation.Loaded -> state.copy(
            isLoading = false,
            detail = mutation.detail,
            error = if (mutation.detail != null) null else state.error
        )
        // Only surface a refresh error when there's no cached detail to show.
        is DetailMutation.RefreshFailed ->
            if (state.detail == null) state.copy(isLoading = false, error = mutation.message) else state
    }
}
