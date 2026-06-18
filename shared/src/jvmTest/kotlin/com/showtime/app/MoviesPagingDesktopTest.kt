package com.showtime.app

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.showtime.app.core.network.UnauthorizedHandler
import com.showtime.app.core.network.createHttpClient
import com.showtime.app.core.network.platformEngineClient
import com.showtime.app.data.local.datastore.TokenStore
import com.showtime.app.data.local.datastore.createDataStore
import com.showtime.app.data.local.db.buildDatabase
import com.showtime.app.data.local.entity.MovieEntity
import com.showtime.app.data.paging.CatalogFilter
import com.showtime.app.data.paging.MoviesRemoteMediator
import com.showtime.app.data.remote.api.createMoviesApi
import de.jensklingenberg.ktorfit.ktorfit
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class MoviesPagingDesktopTest {

    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun remoteMediatorFillsRoom(): Unit = runBlocking {
        val client = createHttpClient(
            engineClient = platformEngineClient(),
            tokenStore = TokenStore(createDataStore()),
            onUnauthorized = UnauthorizedHandler { }
        )
        val api = ktorfit { httpClient(client); baseUrl("https://rma.finlab.rs/") }.createMoviesApi()
        val db = buildDatabase()
        try {
            val mediator = MoviesRemoteMediator(api, db, CatalogFilter())
            val state = PagingState<Int, MovieEntity>(
                pages = emptyList(), anchorPosition = null,
                config = PagingConfig(pageSize = 20), leadingPlaceholderCount = 0
            )
            val result = mediator.load(LoadType.REFRESH, state)
            assertTrue(result is RemoteMediator.MediatorResult.Success, "expected Success, got $result")

            val count = db.movieDao().count()
            println("RemoteMediator REFRESH -> movies in Room = $count")
            assertTrue(count >= 20, "expected >= 20 movies written to Room, got $count")
        } finally {
            db.close()
        }
    }
}
