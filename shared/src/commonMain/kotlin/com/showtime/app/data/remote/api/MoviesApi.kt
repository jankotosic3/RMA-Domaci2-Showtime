package com.showtime.app.data.remote.api

import com.showtime.app.data.remote.dto.CastMemberDto
import com.showtime.app.data.remote.dto.GenreDto
import com.showtime.app.data.remote.dto.MovieDetailDto
import com.showtime.app.data.remote.dto.MovieImagesDto
import com.showtime.app.data.remote.dto.MovieListItemDto
import com.showtime.app.data.remote.dto.PaginatedDto
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface MoviesApi {
    @GET("movies")
    suspend fun getMovies(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("query") query: String? = null,
        @Query("genre_id") genreId: Int? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("min_rating") minRating: Float? = null,
        @Query("sort_by") sortBy: String? = null,    // ignore "popularity" per spec
        @Query("sort_order") sortOrder: String? = null
    ): PaginatedDto<MovieListItemDto>

    @GET("movies/{id}")
    suspend fun getMovieDetail(@Path("id") id: String): MovieDetailDto

    @GET("movies/{id}/cast")
    suspend fun getCast(@Path("id") id: String, @Query("page_size") pageSize: Int = 20): PaginatedDto<CastMemberDto>

    @GET("movies/{id}/images")
    suspend fun getImages(@Path("id") id: String, @Query("type") type: String? = null): MovieImagesDto

    @GET("genres")
    suspend fun getGenres(): List<GenreDto>

    @GET("config")
    suspend fun getConfig(): List<ConfigEntryDto>
}

@kotlinx.serialization.Serializable
data class ConfigEntryDto(val key: String, val value: String)
