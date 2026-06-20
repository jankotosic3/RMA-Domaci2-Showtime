package com.showtime.app.feature.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// Display-only — the session was already scored and submitted by QuizStore on finish.
@Composable
fun QuizResultScreen(
    score: Float,
    correct: Int,
    wrong: Int,
    usedSeconds: Int,
    onPlayAgain: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quiz complete!", style = MaterialTheme.typography.headlineSmall)
        Text(
            score.format2(),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text("out of 100.00", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Column(Modifier.padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ResultRow("Correct", correct.toString())
            ResultRow("Wrong", wrong.toString())
            ResultRow("Time used", "${usedSeconds}s")
        }

        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
            Text("Play again")
        }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Done")
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Text(
        "$label: $value",
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    )
}

// 2-decimal formatting without java.lang.String.format (not available in commonMain).
private fun Float.format2(): String {
    val rounded = (this * 100).roundToInt()
    val whole = rounded / 100
    val frac = (rounded % 100).toString().padStart(2, '0')
    return "$whole.$frac"
}
