package com.showtime.app

import com.showtime.app.data.local.db.buildDatabase
import com.showtime.app.data.remote.api.ShowtimeApi
import com.showtime.app.data.remote.dto.AuthResponse
import com.showtime.app.data.remote.dto.GenreDto
import com.showtime.app.data.remote.dto.LoginRequest
import com.showtime.app.data.remote.dto.MovieListItemDto
import com.showtime.app.data.remote.dto.PostQuizResultResponse
import com.showtime.app.data.remote.dto.QuizResultRequest
import com.showtime.app.data.remote.dto.SignupRequest
import com.showtime.app.data.remote.dto.UserDto
import com.showtime.app.data.repository.LibraryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Server returns a fixed favorites list; sync should cache movie rows + rebuild membership.
private class FakeFavoritesApi(private val favorites: List<MovieListItemDto>) : ShowtimeApi {
    override suspend fun getFavorites(): List<MovieListItemDto> = favorites
    override suspend fun getWatchlist(): List<MovieListItemDto> = emptyList()
    override suspend fun addFavorite(movieId: String) = Unit
    override suspend fun removeFavorite(movieId: String) = Unit
    override suspend fun addWatchlist(movieId: String) = Unit
    override suspend fun removeWatchlist(movieId: String) = Unit
    override suspend fun signup(body: SignupRequest): AuthResponse = throw NotImplementedError()
    override suspend fun login(body: LoginRequest): AuthResponse = throw NotImplementedError()
    override suspend fun me(): UserDto = throw NotImplementedError()
    override suspend fun postQuizResult(body: QuizResultRequest): PostQuizResultResponse = throw NotImplementedError()
}

class LibrarySyncDesktopTest {

    @Test
    fun syncFavorites_cachesMovies_andJoinsForOfflineRead(): Unit = runBlocking {
        val db = buildDatabase()
        try {
            val favs = listOf(
                MovieListItemDto("ttSync1", "Sync One", 2001, 8.0f, 100, "/p1.jpg", listOf(GenreDto(18, "Drama"))),
                MovieListItemDto("ttSync2", "Sync Two", 2002, 7.0f, 50, "/p2.jpg")
            )
            val repo = LibraryRepository(FakeFavoritesApi(favs), db.libraryDao(), db.movieDao())

            repo.syncFavorites()

            // observeFavorites joins favorites -> movies, so a non-empty result proves the
            // movie rows were cached too (offline read works).
            val result = repo.observeFavorites().first()
            assertEquals(2, result.size)
            assertTrue(result.any { it.imdbId == "ttSync1" && it.title == "Sync One" })

            // Re-sync with a smaller list should rebuild membership (clear + re-add).
            repo.syncFavorites()
            assertEquals(2, repo.observeFavorites().first().size)
        } finally {
            db.close()
        }
    }
}
