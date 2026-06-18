package com.showtime.app.feature.library

import com.showtime.app.core.result.AppResult
import com.showtime.app.data.repository.LibraryRepository
import com.showtime.app.domain.model.Movie
import kotlinx.coroutines.flow.Flow

class FavoritesStore(private val repo: LibraryRepository) : LibraryListStore() {
    override fun observe(): Flow<List<Movie>> = repo.observeFavorites()
    override suspend fun sync() = repo.syncFavorites()
    override suspend fun removeItem(id: String): AppResult<Unit> = repo.toggleFavorite(id, currentlyFav = true)
}

class WatchlistStore(private val repo: LibraryRepository) : LibraryListStore() {
    override fun observe(): Flow<List<Movie>> = repo.observeWatchlist()
    override suspend fun sync() = repo.syncWatchlist()
    override suspend fun removeItem(id: String): AppResult<Unit> = repo.toggleWatchlist(id, currentlyOn = true)
}
