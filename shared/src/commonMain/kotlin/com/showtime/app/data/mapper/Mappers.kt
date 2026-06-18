package com.showtime.app.data.mapper

import com.showtime.app.data.local.entity.CastEntity
import com.showtime.app.data.local.entity.MovieEntity
import com.showtime.app.data.local.entity.MovieImageEntity
import com.showtime.app.data.remote.dto.CastMemberDto
import com.showtime.app.data.remote.dto.GenreDto
import com.showtime.app.data.remote.dto.MovieDetailDto
import com.showtime.app.data.remote.dto.MovieImagesDto
import com.showtime.app.data.remote.dto.MovieListItemDto
import com.showtime.app.data.remote.dto.UserDto
import com.showtime.app.domain.model.CastMember
import com.showtime.app.domain.model.Genre
import com.showtime.app.domain.model.Movie
import com.showtime.app.domain.model.MovieDetail
import com.showtime.app.domain.model.UserProfile

// ----- Genre CSV: genres are flattened into "id:name,id:name" for the movies table -----

private fun List<GenreDto>.toGenresCsv(): String = joinToString(",") { "${it.id}:${it.name}" }

fun String.parseGenresCsv(): List<Genre> =
    if (isBlank()) emptyList()
    else split(",").mapNotNull { part ->
        val id = part.substringBefore(":").toIntOrNull() ?: return@mapNotNull null
        Genre(id = id, name = part.substringAfter(":"))
    }

// ----- DTO -> Entity -----

fun MovieListItemDto.toEntity(): MovieEntity = MovieEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    genresCsv = genres.toGenresCsv()
)

fun MovieDetailDto.toEntity(): MovieEntity = MovieEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    backdropPath = backdropPath,
    overview = overview,
    runtime = runtime,
    tmdbRating = tmdbRating,
    genresCsv = genres.toGenresCsv()
)

fun CastMemberDto.toCastEntity(movieId: String, ordering: Int): CastEntity = CastEntity(
    movieId = movieId,
    personId = imdbId,
    name = name,
    profilePath = profilePath,
    ordering = ordering
)

fun MovieImagesDto.toImageEntities(movieId: String): List<MovieImageEntity> =
    posters.map { MovieImageEntity(movieId = movieId, filePath = it.filePath, kind = "poster") } +
        backdrops.map { MovieImageEntity(movieId = movieId, filePath = it.filePath, kind = "backdrop") }

// ----- Entity -> Domain -----

fun MovieEntity.toDomain(
    isFavorite: Boolean = false,
    isWatchlisted: Boolean = false
): Movie = Movie(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    genres = genresCsv.parseGenresCsv(),
    isFavorite = isFavorite,
    isWatchlisted = isWatchlisted
)

fun MovieEntity.toDetailDomain(
    cast: List<CastMember>,
    isFavorite: Boolean,
    isWatchlisted: Boolean
): MovieDetail = MovieDetail(
    imdbId = imdbId,
    title = title,
    overview = overview,
    year = year,
    runtime = runtime,
    posterPath = posterPath,
    backdropPath = backdropPath,
    imdbRating = imdbRating,
    tmdbRating = tmdbRating,
    genres = genresCsv.parseGenresCsv(),
    cast = cast,
    isFavorite = isFavorite,
    isWatchlisted = isWatchlisted
)

fun CastEntity.toDomain(): CastMember = CastMember(
    imdbId = personId,
    name = name,
    profilePath = profilePath
)

fun GenreDto.toDomain(): Genre = Genre(id = id, name = name)

fun UserDto.toDomain(): UserProfile = UserProfile(id = id, username = username, fullName = fullName)
