package com.showtime.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MovieListItemDto(
    val imdbId: String,
    val title: String,
    val year: Int? = null,
    val imdbRating: Float? = null,
    val imdbVotes: Int? = null,
    val posterPath: String? = null,
    val genres: List<GenreDto> = emptyList()
)

@Serializable
data class MovieDetailDto(
    val imdbId: String,
    val title: String,
    val overview: String? = null,
    val year: Int? = null,
    val runtime: Int? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val imdbRating: Float? = null,
    val imdbVotes: Int? = null,
    val tmdbRating: Float? = null,
    val genres: List<GenreDto> = emptyList()
)

@Serializable
data class GenreDto(val id: Int, val name: String)

@Serializable
data class CastMemberDto(
    val imdbId: String,
    val name: String,
    val department: String? = null,
    val profilePath: String? = null
)

@Serializable
data class ImageEntryDto(val filePath: String, val width: Int? = null, val height: Int? = null)

@Serializable
data class MovieImagesDto(
    val posters: List<ImageEntryDto> = emptyList(),
    val backdrops: List<ImageEntryDto> = emptyList(),
    val logos: List<ImageEntryDto> = emptyList()
)

@Serializable
data class PaginatedDto<T>(
    val page: Int, val pageSize: Int, val totalItems: Int,
    val totalPages: Int, val items: List<T>
)
