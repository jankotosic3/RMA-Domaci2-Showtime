package com.showtime.app.feature.catalog

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.showtime.app.core.mvi.MviStore
import com.showtime.app.data.paging.CatalogFilter
import com.showtime.app.data.repository.MoviesRepository
import com.showtime.app.domain.model.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class CatalogStore(
    private val repo: MoviesRepository
) : MviStore<CatalogState, CatalogIntent, CatalogMutation, CatalogEffect>(
    initialState = CatalogState(),
    reducer = catalogReducer
) {
    // The paged stream is its own flow (PagingData is NOT part of ViewState).
    // It re-pages whenever the filter changes.
    private val _filter = MutableStateFlow(CatalogFilter())

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedMovies: Flow<PagingData<Movie>> = _filter
        .flatMapLatest { repo.pagedMovies(it) }
        .cachedIn(viewModelScope)

    init {
        onIntent(CatalogIntent.ScreenOpened)
    }

    override fun onIntent(intent: CatalogIntent) {
        when (intent) {
            CatalogIntent.ScreenOpened -> loadGenres()
            is CatalogIntent.QueryChanged ->
                applyFilter(currentFilter().copy(query = intent.q.trim().ifBlank { null }))
            is CatalogIntent.GenrePicked ->
                applyFilter(currentFilter().copy(genreId = intent.id))
            is CatalogIntent.YearRangeChanged ->
                applyFilter(currentFilter().copy(minYear = intent.min, maxYear = intent.max))
            is CatalogIntent.MinRatingChanged ->
                applyFilter(currentFilter().copy(minRating = intent.r))
            is CatalogIntent.SortChanged ->
                applyFilter(currentFilter().copy(sortBy = intent.sortBy))
            CatalogIntent.OpenFilter -> mutate(CatalogMutation.SheetVisibility(true))
            CatalogIntent.CloseFilter -> mutate(CatalogMutation.SheetVisibility(false))
            CatalogIntent.ClearFilters -> applyFilter(CatalogFilter())
            is CatalogIntent.MovieClicked -> sendEffect(CatalogEffect.OpenDetail(intent.id))
        }
    }

    private fun currentFilter(): CatalogFilter = state.value.filter

    private fun applyFilter(filter: CatalogFilter) {
        _filter.value = filter          // drives re-paging
        mutate(CatalogMutation.FilterUpdated(filter))   // mirror into State for the UI
    }

    private fun loadGenres() = launch {
        runCatching { repo.getGenres() }
            .onSuccess { mutate(CatalogMutation.GenresLoaded(it)) }
    }
}
