package com.showtime.app.feature.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.showtime.app.core.util.ImageUrl
import com.showtime.app.domain.model.Movie
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onMovieClick: (String) -> Unit,
    store: CatalogStore = koinViewModel()
) {
    val state by store.state.collectAsStateWithLifecycle()
    val movies = store.pagedMovies.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        store.effects.collect { effect ->
            when (effect) {
                is CatalogEffect.OpenDetail -> onMovieClick(effect.id)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catalog") },
                actions = {
                    TextButton(onClick = { store.onIntent(CatalogIntent.OpenFilter) }) { Text("Filter") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.filter.query ?: "",
                onValueChange = { store.onIntent(CatalogIntent.QueryChanged(it)) },
                label = { Text("Search movies") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val refresh = movies.loadState.refresh
            when {
                refresh is LoadState.Loading && movies.itemCount == 0 ->
                    Centered(Modifier.fillMaxSize()) { CircularProgressIndicator() }

                refresh is LoadState.Error && movies.itemCount == 0 ->
                    Centered(Modifier.fillMaxSize()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Couldn't load movies")
                            Button(onClick = { movies.retry() }, modifier = Modifier.padding(top = 8.dp)) {
                                Text("Retry")
                            }
                        }
                    }

                movies.itemCount == 0 ->
                    Centered(Modifier.fillMaxSize()) { Text("No movies found") }

                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(
                        count = movies.itemCount,
                        key = { index -> movies[index]?.imdbId ?: "placeholder-$index" }
                    ) { index ->
                        val movie = movies[index]
                        if (movie != null) {
                            MovieRow(movie) { store.onIntent(CatalogIntent.MovieClicked(movie.imdbId)) }
                        }
                    }
                    when (movies.loadState.append) {
                        is LoadState.Loading -> item {
                            Centered(Modifier.fillMaxWidth().height(64.dp)) { CircularProgressIndicator() }
                        }
                        is LoadState.Error -> item {
                            Centered(Modifier.fillMaxWidth().height(64.dp)) {
                                TextButton(onClick = { movies.retry() }) { Text("Retry") }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }

        if (state.filterSheetOpen) {
            FilterSheet(state = state, onIntent = store::onIntent)
        }
    }
}

@Composable
private fun MovieRow(movie: Movie, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageUrl.of(movie.posterPath, "w185"),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(width = 56.dp, height = 84.dp).clip(RoundedCornerShape(6.dp))
        )
        Column(Modifier.padding(start = 12.dp).fillMaxWidth()) {
            Text(movie.title, style = MaterialTheme.typography.titleMedium)
            Text(
                buildString {
                    movie.year?.let { append(it) }
                    movie.imdbRating?.let {
                        if (isNotEmpty()) append("  •  ")
                        append("★ $it")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(state: CatalogState, onIntent: (CatalogIntent) -> Unit) {
    ModalBottomSheet(onDismissRequest = { onIntent(CatalogIntent.CloseFilter) }) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Filters", style = MaterialTheme.typography.titleLarge)

            Text("Sort by", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortChip("Rating", "imdb_rating", state.filter.sortBy) { onIntent(CatalogIntent.SortChanged(it)) }
                SortChip("Year", "year", state.filter.sortBy) { onIntent(CatalogIntent.SortChanged(it)) }
                SortChip("Title", "title", state.filter.sortBy) { onIntent(CatalogIntent.SortChanged(it)) }
            }

            Text("Min rating", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RatingChip("Any", null, state.filter.minRating) { onIntent(CatalogIntent.MinRatingChanged(it)) }
                RatingChip("6+", 6f, state.filter.minRating) { onIntent(CatalogIntent.MinRatingChanged(it)) }
                RatingChip("7+", 7f, state.filter.minRating) { onIntent(CatalogIntent.MinRatingChanged(it)) }
                RatingChip("8+", 8f, state.filter.minRating) { onIntent(CatalogIntent.MinRatingChanged(it)) }
            }

            if (state.genres.isNotEmpty()) {
                Text("Genre", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.filter.genreId == null,
                            onClick = { onIntent(CatalogIntent.GenrePicked(null)) },
                            label = { Text("All") }
                        )
                    }
                    items(state.genres) { genre ->
                        FilterChip(
                            selected = state.filter.genreId == genre.id,
                            onClick = { onIntent(CatalogIntent.GenrePicked(genre.id)) },
                            label = { Text(genre.name) }
                        )
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onIntent(CatalogIntent.ClearFilters) }) { Text("Clear") }
                Button(onClick = { onIntent(CatalogIntent.CloseFilter) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Done")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortChip(label: String, value: String, selected: String?, onPick: (String) -> Unit) {
    FilterChip(selected = selected == value, onClick = { onPick(value) }, label = { Text(label) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RatingChip(label: String, value: Float?, selected: Float?, onPick: (Float?) -> Unit) {
    FilterChip(selected = selected == value, onClick = { onPick(value) }, label = { Text(label) })
}

@Composable
private fun Centered(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center) { content() }
}
