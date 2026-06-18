package com.showtime.app.feature.library

import com.showtime.app.core.mvi.MviStore
import com.showtime.app.core.result.AppResult
import com.showtime.app.domain.model.Movie
import kotlinx.coroutines.flow.Flow

// Favorites and Watchlist are identical except for which repo methods they call.
// NOTE: ScreenOpened is sent by the screen (LaunchedEffect), NOT from an init block —
// a base-class init runs before the subclass's repo field is assigned, which would NPE.
abstract class LibraryListStore : MviStore<LibraryListState, LibraryListIntent, LibraryListMutation, LibraryListEffect>(
    initialState = LibraryListState(),
    reducer = libraryListReducer
) {
    protected abstract fun observe(): Flow<List<Movie>>
    protected abstract suspend fun sync()
    protected abstract suspend fun removeItem(id: String): AppResult<Unit>

    private var started = false

    override fun onIntent(intent: LibraryListIntent) {
        when (intent) {
            LibraryListIntent.ScreenOpened -> start()
            is LibraryListIntent.Remove -> launch {
                val res = removeItem(intent.id)
                if (res is AppResult.Error) sendEffect(LibraryListEffect.ShowMessage(res.message))
            }
            is LibraryListIntent.MovieClicked -> sendEffect(LibraryListEffect.OpenDetail(intent.id))
        }
    }

    private fun start() {
        if (started) return
        started = true
        mutate(LibraryListMutation.Loading)
        // Observe Room (SSOT) — emits cached rows immediately, so offline read works.
        launch { observe().collect { mutate(LibraryListMutation.Items(it)) } }
        // Pull server truth into Room; on failure show the offline banner (cache) or an error.
        launch {
            runCatching { sync() }.onFailure {
                if (state.value.items.isEmpty()) {
                    mutate(LibraryListMutation.Failed(it.message ?: "Couldn't load"))
                } else {
                    mutate(LibraryListMutation.Offline)
                }
            }
        }
    }
}
