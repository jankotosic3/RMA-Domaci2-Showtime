package com.showtime.app.data.remote.api

import com.showtime.app.data.remote.dto.AuthResponse
import com.showtime.app.data.remote.dto.LoginRequest
import com.showtime.app.data.remote.dto.MovieListItemDto
import com.showtime.app.data.remote.dto.PostQuizResultResponse
import com.showtime.app.data.remote.dto.QuizResultRequest
import com.showtime.app.data.remote.dto.SignupRequest
import com.showtime.app.data.remote.dto.UserDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path

interface ShowtimeApi {
    @POST("auth/signup")
    suspend fun signup(@Body body: SignupRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("me")
    suspend fun me(): UserDto

    @GET("me/favorites")
    suspend fun getFavorites(): List<MovieListItemDto>

    @POST("me/favorites/{movieId}")
    suspend fun addFavorite(@Path("movieId") movieId: String)

    @DELETE("me/favorites/{movieId}")
    suspend fun removeFavorite(@Path("movieId") movieId: String)

    @GET("me/watchlist")
    suspend fun getWatchlist(): List<MovieListItemDto>

    @POST("me/watchlist/{movieId}")
    suspend fun addWatchlist(@Path("movieId") movieId: String)

    @DELETE("me/watchlist/{movieId}")
    suspend fun removeWatchlist(@Path("movieId") movieId: String)

    @POST("leaderboard")
    suspend fun postQuizResult(@Body body: QuizResultRequest): PostQuizResultResponse
}
