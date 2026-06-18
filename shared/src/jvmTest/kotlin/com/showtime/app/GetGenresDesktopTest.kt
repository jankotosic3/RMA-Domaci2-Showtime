package com.showtime.app

import com.showtime.app.core.network.UnauthorizedHandler
import com.showtime.app.core.network.createHttpClient
import com.showtime.app.core.network.platformEngineClient
import com.showtime.app.data.local.datastore.TokenStore
import com.showtime.app.data.local.datastore.createDataStore
import com.showtime.app.data.remote.api.createMoviesApi
import de.jensklingenberg.ktorfit.ktorfit
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

// Live end-to-end check: Ktor client + Ktorfit MoviesApi against https://rma.finlab.rs/.
// Proves the networking stack (engine, content negotiation, auth plugin, Ktorfit codegen) works.
class GetGenresDesktopTest {

    @Test
    fun getGenresReturnsData() = runBlocking {
        val client = createHttpClient(
            engineClient = platformEngineClient(),
            tokenStore = TokenStore(createDataStore()),
            onUnauthorized = UnauthorizedHandler { /* no-op for this test */ }
        )
        val api = ktorfit {
            httpClient(client)
            baseUrl("https://rma.finlab.rs/")
        }.createMoviesApi()

        val genres = api.getGenres()
        println("getGenres() -> ${genres.size} genres; first 3 = ${genres.take(3)}")
        assertTrue(genres.isNotEmpty(), "Expected a non-empty genre list from the server")
    }
}
