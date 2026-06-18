package com.showtime.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val posterPath: String?,
    val backdropPath: String? = null,
    val overview: String? = null,
    val runtime: Int? = null,
    val tmdbRating: Float? = null,
    val genresCsv: String          // "18:Drama,80:Crime" — flattened for simplicity
)

// Favorite/Watchlist as membership tables keyed by movie id.
@Entity(tableName = "favorites")
data class FavoriteEntity(@PrimaryKey val imdbId: String, val addedAt: Long)

@Entity(tableName = "watchlist")
data class WatchlistEntity(@PrimaryKey val imdbId: String, val addedAt: Long)

// Cast cached per movie for the detail screen + quiz lead-actor questions.
@Entity(tableName = "cast", primaryKeys = ["movieId", "personId"])
data class CastEntity(
    val movieId: String, val personId: String,
    val name: String, val profilePath: String?, val ordering: Int
)

// Backdrop/poster image paths cached for quiz "guess the movie".
@Entity(tableName = "movie_images", primaryKeys = ["movieId", "filePath"])
data class MovieImageEntity(
    val movieId: String, val filePath: String, val kind: String  // "poster" | "backdrop"
)

// Local quiz statistics — best score & games played (SSOT for Profile stats).
@Entity(tableName = "quiz_stats")
data class QuizStatsEntity(
    @PrimaryKey val category: Int = 1,
    val bestScore: Float = 0f,
    val gamesPlayed: Int = 0
)

// Paging keys table for RemoteMediator.
@Entity(tableName = "movie_remote_keys")
data class MovieRemoteKeyEntity(
    @PrimaryKey val imdbId: String,
    val prevPage: Int?, val nextPage: Int?
)
