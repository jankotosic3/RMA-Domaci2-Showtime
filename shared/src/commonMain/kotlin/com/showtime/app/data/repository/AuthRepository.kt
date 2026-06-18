package com.showtime.app.data.repository

import com.showtime.app.core.result.AppResult
import com.showtime.app.data.local.datastore.TokenStore
import com.showtime.app.data.local.db.ShowtimeDatabase
import com.showtime.app.data.mapper.toDomain
import com.showtime.app.data.remote.api.ShowtimeApi
import com.showtime.app.data.remote.dto.LoginRequest
import com.showtime.app.data.remote.dto.SignupRequest
import com.showtime.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val api: ShowtimeApi,
    private val tokenStore: TokenStore,
    private val db: ShowtimeDatabase
) {
    val isLoggedIn: Flow<Boolean> = tokenStore.tokenFlow.map { it != null }

    suspend fun signup(fullName: String, username: String, password: String): AppResult<Unit> = try {
        val res = api.signup(SignupRequest(fullName, username, password))
        tokenStore.save(res.accessToken)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(mapAuthError(e))
    }

    suspend fun login(username: String, password: String): AppResult<Unit> = try {
        val res = api.login(LoginRequest(username, password))
        tokenStore.save(res.accessToken)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(mapAuthError(e))
    }

    suspend fun getProfile(): UserProfile = api.me().toDomain()

    // Called by explicit logout AND by the global 401 handler (centralized, no per-screen logic).
    // Clears the token + user-scoped Room data, but KEEPS the global movie catalog.
    suspend fun logout() {
        tokenStore.clear()
        db.libraryDao().clearFavorites()
        db.libraryDao().clearWatchlist()
        db.quizStatsDao().clear()
    }

    private fun mapAuthError(e: Exception): String = when {
        e.message?.contains("409") == true -> "Username already taken"
        e.message?.contains("401") == true -> "Invalid username or password"
        e.message?.contains("400") == true -> "Please check your input"
        else -> "Network error. Try again."
    }
}
