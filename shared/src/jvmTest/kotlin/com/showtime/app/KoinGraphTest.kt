package com.showtime.app

import com.showtime.app.core.network.UnauthorizedHandler
import com.showtime.app.data.local.datastore.TokenStore
import com.showtime.app.data.local.db.ShowtimeDatabase
import com.showtime.app.data.remote.api.MoviesApi
import com.showtime.app.data.remote.api.ShowtimeApi
import com.showtime.app.data.repository.AuthRepository
import com.showtime.app.di.appModules
import io.ktor.client.HttpClient
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

// Proves the Koin object graph actually resolves: every singleton the data layer
// exposes can be constructed, including the dependency chain
// MoviesApi <- Ktorfit <- HttpClient <- (engine, TokenStore, UnauthorizedHandler).
class KoinGraphTest {

    @Test
    fun resolvesCoreGraph() {
        val app = koinApplication { modules(appModules()) }
        val koin = app.koin
        try {
            assertNotNull(koin.get<TokenStore>())
            assertNotNull(koin.get<ShowtimeDatabase>())
            assertNotNull(koin.get<UnauthorizedHandler>())
            assertNotNull(koin.get<HttpClient>())
            assertNotNull(koin.get<MoviesApi>())
            assertNotNull(koin.get<ShowtimeApi>())
            // Step 8: AuthRepository wires (ShowtimeApi, TokenStore, ShowtimeDatabase);
            // the 401 UnauthorizedHandler closes over it.
            assertNotNull(koin.get<AuthRepository>())
        } finally {
            app.close()
        }
    }
}
