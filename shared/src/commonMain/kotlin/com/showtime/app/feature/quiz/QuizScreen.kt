package com.showtime.app.feature.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel

// BackHandler is still the multiplatform way to intercept system back (its NavigationEventHandler
// replacement isn't on the commonMain classpath here); opt in and silence the deprecation notice.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
@Composable
fun QuizScreen(
    onFinished: (score: Float, correct: Int, wrong: Int, usedSeconds: Int) -> Unit,
    onAbandon: () -> Unit,
    store: QuizStore = koinViewModel()
) {
    val state by store.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { store.onIntent(QuizIntent.Start) }
    LaunchedEffect(Unit) {
        store.effects.collect { effect ->
            when (effect) {
                is QuizEffect.NavigateToResult ->
                    onFinished(effect.score, effect.correct, effect.wrong, effect.usedSeconds)
                QuizEffect.NavigateBack -> onAbandon()
            }
        }
    }

    // Intercept system back during play → abandon-confirm dialog (spec). No-op on Desktop.
    BackHandler(enabled = state.phase == QuizState.Phase.Playing) {
        store.onIntent(QuizIntent.BackPressed)
    }

    if (state.showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { store.onIntent(QuizIntent.DismissAbandon) },
            title = { Text("Quit quiz?") },
            text = { Text("Your progress won't be saved and this game won't count.") },
            confirmButton = {
                TextButton(onClick = { store.onIntent(QuizIntent.ConfirmAbandon) }) { Text("Quit") }
            },
            dismissButton = {
                TextButton(onClick = { store.onIntent(QuizIntent.DismissAbandon) }) { Text("Keep playing") }
            }
        )
    }

    Scaffold(
        topBar = {
            // No navigationIcon — the Up affordance is intentionally hidden during the quiz.
            TopAppBar(
                title = {
                    val n = state.questions.size
                    Text(if (n > 0) "Question ${state.index + 1} / $n" else "Quiz")
                },
                actions = {
                    if (state.phase == QuizState.Phase.Playing) {
                        TextButton(onClick = { store.onIntent(QuizIntent.BackPressed) }) { Text("Quit") }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.phase) {
                QuizState.Phase.Loading, QuizState.Phase.Finished ->
                    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

                QuizState.Phase.NotEnoughData ->
                    Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                        Text(
                            "Browse the catalog first to populate your quiz pool.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }

                QuizState.Phase.Playing -> PlayingContent(state, onAnswer = { store.onIntent(QuizIntent.Answer(it)) })
            }
        }
    }
}

@Composable
private fun PlayingContent(state: QuizState, onAnswer: (Int) -> Unit) {
    val question = state.current ?: return

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Timer
        Text(
            "⏱ ${state.remainingSeconds}s",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
        LinearProgressIndicator(
            progress = { state.remainingSeconds.toFloat() / QuizScoring.MAX_TIME },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Text(promptFor(question.type), style = MaterialTheme.typography.titleMedium)

        question.promptImageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().height(220.dp)
                    .padding(vertical = 12.dp).clip(RoundedCornerShape(12.dp))
            )
        }

        question.promptTitle?.let { title ->
            Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            question.options.forEachIndexed { i, option ->
                OptionButton(
                    text = option,
                    state = optionState(state, i, question.correctIndex),
                    enabled = !state.revealed,
                    onClick = { onAnswer(i) }
                )
            }
        }
    }
}

private enum class OptionVisual { NEUTRAL, CORRECT, WRONG }

private fun optionState(state: QuizState, index: Int, correctIndex: Int): OptionVisual = when {
    !state.revealed -> OptionVisual.NEUTRAL
    index == correctIndex -> OptionVisual.CORRECT
    index == state.selectedIndex -> OptionVisual.WRONG
    else -> OptionVisual.NEUTRAL
}

@Composable
private fun OptionButton(text: String, state: OptionVisual, enabled: Boolean, onClick: () -> Unit) {
    val colors = when (state) {
        OptionVisual.NEUTRAL -> ButtonDefaults.buttonColors()
        OptionVisual.CORRECT -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
        OptionVisual.WRONG -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    }
    Button(
        onClick = onClick,
        enabled = enabled || state != OptionVisual.NEUTRAL,
        colors = colors,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text, textAlign = TextAlign.Center)
    }
}

private fun promptFor(type: QuizType): String = when (type) {
    QuizType.GUESS_MOVIE -> "Which movie is this?"
    QuizType.GUESS_YEAR -> "What year was it released?"
    QuizType.GUESS_LEAD_ACTOR -> "Who stars in this movie?"
}
