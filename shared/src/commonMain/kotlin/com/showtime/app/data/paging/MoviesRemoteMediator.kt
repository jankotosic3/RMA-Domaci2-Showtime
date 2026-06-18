package com.showtime.app.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.showtime.app.data.local.db.ShowtimeDatabase
import com.showtime.app.data.local.entity.MovieEntity
import com.showtime.app.data.local.entity.MovieRemoteKeyEntity
import com.showtime.app.data.mapper.toEntity
import com.showtime.app.data.remote.api.MoviesApi

data class CatalogFilter(
    val query: String? = null,
    val genreId: Int? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minRating: Float? = null,
    val sortBy: String? = "imdb_rating"
)

@OptIn(ExperimentalPagingApi::class)
class MoviesRemoteMediator(
    private val api: MoviesApi,
    private val db: ShowtimeDatabase,
    private val filter: CatalogFilter
) : RemoteMediator<Int, MovieEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastId = state.lastItemOrNull()?.imdbId
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                db.remoteKeyDao().keyFor(lastId)?.nextPage
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return try {
            val response = api.getMovies(
                page = page, pageSize = 20,
                query = filter.query, genreId = filter.genreId,
                minYear = filter.minYear, maxYear = filter.maxYear,
                minRating = filter.minRating, sortBy = filter.sortBy,
                sortOrder = "desc"
            )
            val endReached = response.page >= response.totalPages
            val entities = response.items.map { it.toEntity() }

            // NOTE: the plan wrapped these writes in db.useWriterConnection { ... }, but calling
            // suspend DAOs inside that block risks a connection deadlock in Room KMP. We write
            // sequentially instead; REFRESH clears the keys first so partial writes self-heal.
            if (loadType == LoadType.REFRESH) {
                db.remoteKeyDao().clear()
            }
            db.movieDao().upsertAll(entities)
            db.remoteKeyDao().upsertAll(
                entities.map {
                    MovieRemoteKeyEntity(
                        imdbId = it.imdbId,
                        prevPage = if (page == 1) null else page - 1,
                        nextPage = if (endReached) null else page + 1
                    )
                }
            )
            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
