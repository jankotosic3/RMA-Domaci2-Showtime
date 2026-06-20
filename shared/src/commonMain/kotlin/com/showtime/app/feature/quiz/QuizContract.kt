package com.showtime.app.feature.quiz

import com.showtime.app.core.mvi.Effect
import com.showtime.app.core.mvi.Intent
import com.showtime.app.core.mvi.Mutation
import com.showtime.app.core.mvi.Reducer
import com.showtime.app.core.mvi.ViewState

data class QuizState(
    val phase: Phase = Phase.Loading,
    val questions: List<QuizQuestion> = emptyList(),
    val index: Int = 0,
    val selectedIndex: Int? = null,           // set on answer to reveal correctness
    val revealed: Boolean = false,
    val correctCount: Int = 0,
    val remainingSeconds: Int = QuizScoring.MAX_TIME,
    val showAbandonDialog: Boolean = false
) : ViewState {
    enum class Phase { Loading, NotEnoughData, Playing, Finished }

    val current: QuizQuestion? get() = questions.getOrNull(index)
}

sealed interface QuizIntent : Intent {
    data object Start : QuizIntent
    data class Answer(val optionIndex: Int) : QuizIntent
    data object BackPressed : QuizIntent
    data object ConfirmAbandon : QuizIntent
    data object DismissAbandon : QuizIntent
    data object TimerTick : QuizIntent
}

sealed interface QuizMutation : Mutation {
    data object Loading : QuizMutation
    data object NotEnoughData : QuizMutation
    data class Started(val questions: List<QuizQuestion>) : QuizMutation
    data object Tick : QuizMutation
    data class Reveal(val selectedIndex: Int, val wasCorrect: Boolean) : QuizMutation
    data object Advance : QuizMutation
    data object Finished : QuizMutation
    data object ShowAbandon : QuizMutation
    data object HideAbandon : QuizMutation
}

sealed interface QuizEffect : Effect {
    data class NavigateToResult(
        val score: Float, val correct: Int, val wrong: Int, val usedSeconds: Int
    ) : QuizEffect
    data object NavigateBack : QuizEffect
}

// Pure reducer — no IO, no coroutines.
val quizReducer = Reducer<QuizState, QuizMutation> { state, mutation ->
    when (mutation) {
        QuizMutation.Loading -> state.copy(phase = QuizState.Phase.Loading)
        QuizMutation.NotEnoughData -> state.copy(phase = QuizState.Phase.NotEnoughData)
        is QuizMutation.Started -> state.copy(
            phase = QuizState.Phase.Playing,
            questions = mutation.questions,
            index = 0,
            selectedIndex = null,
            revealed = false,
            correctCount = 0,
            remainingSeconds = QuizScoring.MAX_TIME
        )
        QuizMutation.Tick -> state.copy(remainingSeconds = (state.remainingSeconds - 1).coerceAtLeast(0))
        is QuizMutation.Reveal -> state.copy(
            selectedIndex = mutation.selectedIndex,
            revealed = true,
            correctCount = state.correctCount + if (mutation.wasCorrect) 1 else 0
        )
        QuizMutation.Advance -> state.copy(
            index = state.index + 1,
            selectedIndex = null,
            revealed = false
        )
        QuizMutation.Finished -> state.copy(phase = QuizState.Phase.Finished)
        QuizMutation.ShowAbandon -> state.copy(showAbandonDialog = true)
        QuizMutation.HideAbandon -> state.copy(showAbandonDialog = false)
    }
}
