package com.showtime.app

import com.showtime.app.core.result.AppResult
import com.showtime.app.data.local.db.buildDatabase
import com.showtime.app.data.remote.api.ShowtimeApi
import com.showtime.app.data.remote.dto.AuthResponse
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// A ShowtimeApi whose favorite/watchlist writes always fail, to exercise rollback.
private class FailingApi : ShowtimeApi {
    override suspend fun addFavorite(movieId: String) = throw RuntimeException("server boom")
    override suspend fun removeFavorite(movieId: String) = throw RuntimeException("server boom")
    override suspend fun addWatchlist(movieId: String) = throw RuntimeException("server boom")
    override suspend fun removeWatchlist(movieId: String) = throw RuntimeException("server boom")
    override suspend fun signup(body: SignupRequest): AuthResponse = throw NotImplementedError()
    override suspend fun login(body: LoginRequest): AuthResponse = throw NotImplementedError()
    override suspend fun me(): UserDto = throw NotImplementedError()
    override suspend fun getFavorites(): List<MovieListItemDto> = throw NotImplementedError()
    override suspend fun getWatchlist(): List<MovieListItemDto> = throw NotImplementedError()
    override suspend fun postQuizResult(body: QuizResultRequest): PostQuizResultResponse = throw NotImplementedError()
}

class LibraryRollbackDesktopTest {

    @Test
    fun optimisticFavorite_rollsBack_whenServerFails(): Unit = runBlocking {
        val db = buildDatabase()
        try {
            val repo = LibraryRepository(FailingApi(), db.libraryDao(), db.movieDao())
            val id = "tt_rollback_test"
            db.libraryDao().removeFavorite(id) // clean slate

            // currently not a favorite → optimistic add, server fails → must roll back to "not favorite"
            val res = repo.toggleFavorite(id, currentlyFav = false)
            assertTrue(res is AppResult.Error, "expected Error, got $res")
            assertFalse(db.libraryDao().isFavorite(id).first(), "favorite should have rolled back to false")
        } finally {
            db.close()
        }
    }
}
