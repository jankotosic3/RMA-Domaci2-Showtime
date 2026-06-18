package com.showtime.app.di

import com.showtime.app.core.network.UnauthorizedHandler
import com.showtime.app.core.network.createHttpClient
import com.showtime.app.core.network.platformEngineClient
import com.showtime.app.data.local.datastore.TokenStore
import com.showtime.app.data.local.datastore.createDataStore
import com.showtime.app.data.local.db.buildDatabase
import com.showtime.app.data.remote.api.createMoviesApi
import com.showtime.app.data.remote.api.createShowtimeApi
import com.showtime.app.data.repository.AuthRepository
import com.showtime.app.data.repository.LibraryRepository
import com.showtime.app.data.repository.MovieDetailRepository
import com.showtime.app.data.repository.MoviesRepository
import com.showtime.app.data.local.db.ShowtimeDatabase
import com.showtime.app.feature.auth.AuthLandingStore
import com.showtime.app.feature.auth.LoginStore
import com.showtime.app.feature.auth.SignupStore
import com.showtime.app.feature.catalog.CatalogStore
import com.showtime.app.feature.detail.DetailStore
import com.showtime.app.feature.library.FavoritesStore
import com.showtime.app.feature.library.WatchlistStore
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfit
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single { TokenStore(createDataStore()) }
    single { buildDatabase() }

    // Centralized 401 → forced logout. Any 401 from any request triggers this, which clears
    // the token + user-scoped data; isLoggedIn then flips and App swaps to the auth graph.
    single<UnauthorizedHandler> { UnauthorizedHandler { get<AuthRepository>().logout() } }

    // The bare OkHttp engine client (platformEngineClient()) is inlined here rather than
    // registered as its own `single`: it is *also* an HttpClient, and a second HttpClient
    // definition makes createHttpClient's `engineClient = get()` resolve to itself (cycle).
    single {
        createHttpClient(
            engineClient = platformEngineClient(),
            tokenStore = get(),
            onUnauthorized = get()
        )
    }
    single { ktorfit { httpClient(get<HttpClient>()); baseUrl("https://rma.finlab.rs/") } }
    single { get<Ktorfit>().createMoviesApi() }
    single { get<Ktorfit>().createShowtimeApi() }

    single { AuthRepository(get(), get(), get()) }
    single { MoviesRepository(get(), get()) }
    single { MovieDetailRepository(get(), get()) }
    single { LibraryRepository(get(), get<ShowtimeDatabase>().libraryDao(), get<ShowtimeDatabase>().movieDao()) }
}

// Feature view-models are appended here as each feature lands (Steps 8–15).
val featureModule = module {
    viewModel { AuthLandingStore() }
    viewModel { LoginStore(get()) }
    viewModel { SignupStore(get()) }
    viewModel { CatalogStore(get()) }
    viewModel { (movieId: String) -> DetailStore(get(), get(), movieId) }
    viewModel { FavoritesStore(get()) }
    viewModel { WatchlistStore(get()) }
}

fun appModules() = listOf(dataModule, featureModule)
