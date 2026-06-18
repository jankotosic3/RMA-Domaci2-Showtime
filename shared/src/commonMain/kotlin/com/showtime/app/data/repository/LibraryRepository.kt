package com.showtime.app.data.repository

import com.showtime.app.core.result.AppResult
import com.showtime.app.core.util.nowEpochMillis
import com.showtime.app.data.local.db.LibraryDao
import com.showtime.app.data.local.db.MovieDao
import com.showtime.app.data.local.entity.FavoriteEntity
import com.showtime.app.data.local.entity.WatchlistEntity
import com.showtime.app.data.mapper.toDomain
import com.showtime.app.data.mapper.toEntity
import com.showtime.app.data.remote.api.ShowtimeApi
import com.showtime.app.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryRepository(
    private val api: ShowtimeApi,
    private val dao: LibraryDao,
    // Needed so sync can cache the movie rows themselves (titles/posters) for offline display —
    // the plan's 2-arg constructor couldn't satisfy its own "also upsert MovieEntity" note.
    private val movieDao: MovieDao
) {
    fun observeFavorites(): Flow<List<Movie>> = dao.observeFavoriteMovies().map { list -> list.map { it.toDomain() } }
    fun observeWatchlist(): Flow<List<Movie>> = dao.observeWatchlistMovies().map { list -> list.map { it.toDomain() } }
    fun observeFavoritesCount(): Flow<Int> = dao.favoritesCount()
    fun observeWatchlistCount(): Flow<Int> = dao.watchlistCount()

    private fun now() = nowEpochMillis()

    // Optimistic: write Room first (UI flips instantly via the Room flow), call the server,
    // and roll the Room write back if the server rejects it.
    suspend fun toggleFavorite(id: String, currentlyFav: Boolean): AppResult<Unit> {
        if (currentlyFav) dao.removeFavorite(id) else dao.addFavorite(FavoriteEntity(id, now()))
        return try {
            if (currentlyFav) api.removeFavorite(id) else api.addFavorite(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            if (currentlyFav) dao.addFavorite(FavoriteEntity(id, now())) else dao.removeFavorite(id)
            AppResult.Error(e.message ?: "Could not update favorites")
        }
    }

    suspend fun toggleWatchlist(id: String, currentlyOn: Boolean): AppResult<Unit> {
        if (currentlyOn) dao.removeWatchlist(id) else dao.addWatchlist(WatchlistEntity(id, now()))
        return try {
            if (currentlyOn) api.removeWatchlist(id) else api.addWatchlist(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            if (currentlyOn) dao.addWatchlist(WatchlistEntity(id, now())) else dao.removeWatchlist(id)
            AppResult.Error(e.message ?: "Could not update watchlist")
        }
    }

    // Pull server truth into Room. Caches the movie rows too, then rebuilds the membership
    // table preserving server order (addedAt descending == server list order).
    suspend fun syncFavorites() {
        val favs = api.getFavorites()
        movieDao.upsertAll(favs.map { it.toEntity() })
        dao.clearFavorites()
        val base = now()
        favs.forEachIndexed { i, m -> dao.addFavorite(FavoriteEntity(m.imdbId, base - i)) }
    }

    suspend fun syncWatchlist() {
        val items = api.getWatchlist()
        movieDao.upsertAll(items.map { it.toEntity() })
        dao.clearWatchlist()
        val base = now()
        items.forEachIndexed { i, m -> dao.addWatchlist(WatchlistEntity(m.imdbId, base - i)) }
    }
}
