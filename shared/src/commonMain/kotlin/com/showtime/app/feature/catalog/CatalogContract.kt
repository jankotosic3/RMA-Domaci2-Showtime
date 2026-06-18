package com.showtime.app.feature.catalog

import com.showtime.app.core.mvi.Effect
import com.showtime.app.core.mvi.Intent
import com.showtime.app.core.mvi.Mutation
import com.showtime.app.core.mvi.Reducer
import com.showtime.app.core.mvi.ViewState
import com.showtime.app.data.paging.CatalogFilter
import com.showtime.app.domain.model.Genre

data class CatalogState(
    val filter: CatalogFilter = CatalogFilter(),
    val genres: List<Genre> = emptyList(),
    val filterSheetOpen: Boolean = false
) : ViewState

sealed interface CatalogIntent : Intent {
    data object ScreenOpened : CatalogIntent
    data class QueryChanged(val q: String) : CatalogIntent
    data class GenrePicked(val id: Int?) : CatalogIntent
    data class YearRangeChanged(val min: Int?, val max: Int?) : CatalogIntent
    data class MinRatingChanged(val r: Float?) : CatalogIntent
    data class SortChanged(val sortBy: String) : CatalogIntent   // year/title/imdb_rating
    data object OpenFilter : CatalogIntent
    data object CloseFilter : CatalogIntent
    data object ClearFilters : CatalogIntent
    data class MovieClicked(val id: String) : CatalogIntent
}

sealed interface CatalogMutation : Mutation {
    data class GenresLoaded(val genres: List<Genre>) : CatalogMutation
    data class FilterUpdated(val filter: CatalogFilter) : CatalogMutation
    data class SheetVisibility(val open: Boolean) : CatalogMutation
}

sealed interface CatalogEffect : Effect {
    data class OpenDetail(val id: String) : CatalogEffect
}

val catalogReducer = Reducer<CatalogState, CatalogMutation> { state, mutation ->
    when (mutation) {
        is CatalogMutation.GenresLoaded -> state.copy(genres = mutation.genres)
        is CatalogMutation.FilterUpdated -> state.copy(filter = mutation.filter)
        is CatalogMutation.SheetVisibility -> state.copy(filterSheetOpen = mutation.open)
    }
}
