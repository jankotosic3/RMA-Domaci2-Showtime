package com.showtime.app.domain.model

data class Movie(
    val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val posterPath: String?,
    val genres: List<Genre>,
    val isFavorite: Boolean = false,
    val isWatchlisted: Boolean = false
)

data class MovieDetail(
    val imdbId: String,
    val title: String,
    val overview: String?,
    val year: Int?,
    val runtime: Int?,
    val posterPath: String?,
    val backdropPath: String?,
    val imdbRating: Float?,
    val tmdbRating: Float?,
    val genres: List<Genre>,
    val cast: List<CastMember>,
    val isFavorite: Boolean,
    val isWatchlisted: Boolean
)

data class Genre(val id: Int, val name: String)
data class CastMember(val imdbId: String, val name: String, val profilePath: String?)
data class UserProfile(val id: Int, val username: String, val fullName: String)
data class QuizStats(val bestScore: Float, val gamesPlayed: Int)
