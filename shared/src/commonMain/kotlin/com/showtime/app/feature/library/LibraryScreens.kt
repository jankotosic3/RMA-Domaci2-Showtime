package com.showtime.app.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.showtime.app.core.util.ImageUrl
import com.showtime.app.domain.model.Movie
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(onMovieClick: (String) -> Unit, store: FavoritesStore = koinViewModel()) {
    LibraryListContent("Favorites", "No favorites yet", store, onMovieClick)
}

@Composable
fun WatchlistScreen(onMovieClick: (String) -> Unit, store: WatchlistStore = koinViewModel()) {
    LibraryListContent("Watchlist", "Your watchlist is empty", store, onMovieClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryListContent(
    title: String,
    emptyText: String,
    store: LibraryListStore,
    onMovieClick: (String) -> Unit
) {
    val state by store.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { store.onIntent(LibraryListIntent.ScreenOpened) }
    LaunchedEffect(Unit) {
        store.effects.collect { effect ->
            when (effect) {
                is LibraryListEffect.OpenDetail -> onMovieClick(effect.id)
                is LibraryListEffect.ShowMessage -> snackbar.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.items.isEmpty() ->
                    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

                state.error != null && state.items.isEmpty() ->
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error!!)
                            Button(
                                onClick = { store.onIntent(LibraryListIntent.ScreenOpened) },
                                modifier = Modifier.padding(top = 8.dp)
                            ) { Text("Retry") }
                        }
                    }

                state.items.isEmpty() ->
                    Box(Modifier.fillMaxSize(), Alignment.Center) { Text(emptyText) }

                else -> Column(Modifier.fillMaxSize()) {
                    if (state.isOffline) {
                        Text(
                            "Offline — showing saved items",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(state.items, key = { it.imdbId }) { movie ->
                            LibraryRow(
                                movie = movie,
                                onClick = { store.onIntent(LibraryListIntent.MovieClicked(movie.imdbId)) },
                                onRemove = { store.onIntent(LibraryListIntent.Remove(movie.imdbId)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(movie: Movie, onClick: () -> Unit, onRemove: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageUrl.of(movie.posterPath, "w185"),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(width = 48.dp, height = 72.dp).clip(RoundedCornerShape(6.dp))
        )
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
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
        TextButton(onClick = onRemove) { Text("Remove") }
    }
}
