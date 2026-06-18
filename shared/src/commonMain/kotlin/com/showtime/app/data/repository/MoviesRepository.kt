package com.showtime.app.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.showtime.app.data.local.db.ShowtimeDatabase
import com.showtime.app.data.mapper.toDomain
import com.showtime.app.data.mapper.toEntity
import com.showtime.app.data.paging.CatalogFilter
import com.showtime.app.data.paging.MoviesRemoteMediator
import com.showtime.app.data.remote.api.MoviesApi
import com.showtime.app.domain.model.Genre
import com.showtime.app.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MoviesRepository(
    private val api: MoviesApi,
    private val db: ShowtimeDatabase
) {
    @OptIn(ExperimentalPagingApi::class)
    fun pagedMovies(filter: CatalogFilter): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(pageSize = 20, prefetchDistance = 5),
        remoteMediator = MoviesRemoteMediator(api, db, filter),
        pagingSourceFactory = {
            db.movieDao().pagingSource(
                filter.query, filter.genreId, filter.minYear,
                filter.maxYear, filter.minRating, filter.sortBy
            )
        }
    ).flow.map { paging -> paging.map { it.toDomain() } }

    // Pull top movies so the quiz pool requirement (Step 14) can be satisfied later.
    suspend fun bootstrapTopMovies() {
        val response = api.getMovies(page = 1, pageSize = 100, sortBy = "imdb_votes", sortOrder = "desc")
        db.movieDao().upsertAll(response.items.map { it.toEntity() })
    }

    suspend fun getGenres(): List<Genre> = api.getGenres().map { it.toDomain() }
}
