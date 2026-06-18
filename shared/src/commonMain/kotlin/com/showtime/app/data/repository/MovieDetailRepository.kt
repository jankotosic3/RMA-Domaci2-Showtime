package com.showtime.app.data.repository

import com.showtime.app.data.local.db.ShowtimeDatabase
import com.showtime.app.data.mapper.toCastEntity
import com.showtime.app.data.mapper.toDetailDomain
import com.showtime.app.data.mapper.toDomain
import com.showtime.app.data.mapper.toEntity
import com.showtime.app.data.mapper.toImageEntities
import com.showtime.app.data.remote.api.MoviesApi
import com.showtime.app.domain.model.MovieDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class MovieDetailRepository(
    private val api: MoviesApi,
    private val db: ShowtimeDatabase
) {
    // The detail screen is fully Room-sourced: movie + cast + live favorite/watchlist flags.
    fun observeDetail(id: String): Flow<MovieDetail?> = combine(
        db.movieDao().observeMovie(id),
        db.castImageDao().observeCast(id),
        db.libraryDao().isFavorite(id),
        db.libraryDao().isWatchlisted(id)
    ) { movie, cast, fav, watch ->
        movie?.toDetailDomain(cast = cast.map { it.toDomain() }, isFavorite = fav, isWatchlisted = watch)
    }

    // Fetch fresh data into Room; the observeDetail flow then re-emits automatically.
    suspend fun refresh(id: String) {
        val detail = api.getMovieDetail(id)
        db.movieDao().upsert(detail.toEntity())
        val cast = api.getCast(id, pageSize = 20).items
        db.castImageDao().upsertCast(cast.mapIndexed { i, c -> c.toCastEntity(id, i) })
        val images = api.getImages(id)
        db.castImageDao().upsertImages(images.toImageEntities(id))
    }
}
