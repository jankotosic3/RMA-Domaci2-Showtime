package com.showtime.app.feature.library

import com.showtime.app.core.mvi.Effect
import com.showtime.app.core.mvi.Intent
import com.showtime.app.core.mvi.Mutation
import com.showtime.app.core.mvi.Reducer
import com.showtime.app.core.mvi.ViewState
import com.showtime.app.domain.model.Movie

data class LibraryListState(
    val isLoading: Boolean = true,
    val items: List<Movie> = emptyList(),
    val isOffline: Boolean = false,    // sync failed but Room had cached rows
    val error: String? = null          // sync failed AND nothing cached
) : ViewState

sealed interface LibraryListIntent : Intent {
    data object ScreenOpened : LibraryListIntent
    data class Remove(val id: String) : LibraryListIntent
    data class MovieClicked(val id: String) : LibraryListIntent
}

sealed interface LibraryListMutation : Mutation {
    data object Loading : LibraryListMutation
    data class Items(val items: List<Movie>) : LibraryListMutation
    data object Offline : LibraryListMutation
    data class Failed(val message: String) : LibraryListMutation
}

sealed interface LibraryListEffect : Effect {
    data class OpenDetail(val id: String) : LibraryListEffect
    data class ShowMessage(val message: String) : LibraryListEffect
}

val libraryListReducer = Reducer<LibraryListState, LibraryListMutation> { state, mutation ->
    when (mutation) {
        LibraryListMutation.Loading -> state.copy(isLoading = true, error = null)
        is LibraryListMutation.Items -> state.copy(isLoading = false, items = mutation.items)
        LibraryListMutation.Offline -> state.copy(isLoading = false, isOffline = true)
        is LibraryListMutation.Failed -> state.copy(isLoading = false, error = mutation.message)
    }
}
