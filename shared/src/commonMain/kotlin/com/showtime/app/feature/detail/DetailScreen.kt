package com.showtime.app.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.showtime.app.core.util.ImageUrl
import com.showtime.app.domain.model.MovieDetail
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    movieId: String,
    onBack: () -> Unit,
    store: DetailStore = koinViewModel { parametersOf(movieId) }
) {
    val state by store.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        store.effects.collect { effect ->
            when (effect) {
                is DetailEffect.ShowMessage -> snackbar.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.detail?.title ?: "Details", maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                state.isLoading && state.detail == null -> CircularProgressIndicator()
                state.error != null && state.detail == null -> Text(state.error!!)
                state.detail != null -> DetailContent(
                    detail = state.detail!!,
                    onToggleFavorite = { store.onIntent(DetailIntent.ToggleFavorite(it)) },
                    onToggleWatchlist = { store.onIntent(DetailIntent.ToggleWatchlist(it)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailContent(
    detail: MovieDetail,
    onToggleFavorite: (Boolean) -> Unit,
    onToggleWatchlist: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        AsyncImage(
            model = ImageUrl.of(detail.backdropPath ?: detail.posterPath, "w780"),
            contentDescription = detail.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )

        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            AsyncImage(
                model = ImageUrl.of(detail.posterPath, "w342"),
                contentDescription = detail.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 100.dp, height = 150.dp).clip(RoundedCornerShape(8.dp))
            )
            Column(Modifier.padding(start = 16.dp)) {
                Text(detail.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    buildString {
                        detail.year?.let { append(it) }
                        detail.runtime?.let {
                            if (isNotEmpty()) append("  •  ")
                            append("${it}m")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    detail.imdbRating?.let { Text("IMDb ★ $it", style = MaterialTheme.typography.bodyMedium) }
                    detail.tmdbRating?.let { Text("TMDB ★ $it", style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }

        if (detail.genres.isNotEmpty()) {
            LazyRow(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(detail.genres) { genre ->
                    AssistChip(onClick = {}, label = { Text(genre.name) })
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (detail.isFavorite) {
                FilledTonalButton(onClick = { onToggleFavorite(true) }, modifier = Modifier.weight(1f)) {
                    Text("♥ In Favorites")
                }
            } else {
                OutlinedButton(onClick = { onToggleFavorite(false) }, modifier = Modifier.weight(1f)) {
                    Text("♡ Favorite")
                }
            }
            if (detail.isWatchlisted) {
                FilledTonalButton(onClick = { onToggleWatchlist(true) }, modifier = Modifier.weight(1f)) {
                    Text("🔖 In Watchlist")
                }
            } else {
                OutlinedButton(onClick = { onToggleWatchlist(false) }, modifier = Modifier.weight(1f)) {
                    Text("＋ Watchlist")
                }
            }
        }

        detail.overview?.takeIf { it.isNotBlank() }?.let { overview ->
            Text("Overview", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            Text(overview, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
        }

        if (detail.cast.isNotEmpty()) {
            Text("Cast", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            LazyRow(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(detail.cast) { member ->
                    Column(Modifier.width(80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = ImageUrl.of(member.profilePath, "w185"),
                            contentDescription = member.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(36.dp))
                        )
                        Text(
                            member.name,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
