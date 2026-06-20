package com.showtime.app.feature.quiz

import androidx.lifecycle.viewModelScope
import com.showtime.app.core.mvi.MviStore
import com.showtime.app.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuizStore(
    private val repo: QuizRepository,
    private val generator: QuizGenerator
) : MviStore<QuizState, QuizIntent, QuizMutation, QuizEffect>(
    initialState = QuizState(),
    reducer = quizReducer
) {
    private var timerJob: Job? = null
    private var finished = false
    private var started = false   // guards against a re-Start (e.g. rotation) restarting the session

    override fun onIntent(intent: QuizIntent) {
        when (intent) {
            QuizIntent.Start -> start()
            is QuizIntent.Answer -> answer(intent.optionIndex)
            QuizIntent.BackPressed -> mutate(QuizMutation.ShowAbandon)
            QuizIntent.ConfirmAbandon -> abandon()
            QuizIntent.DismissAbandon -> mutate(QuizMutation.HideAbandon)
            QuizIntent.TimerTick -> tick()
        }
    }

    private fun start() {
        if (started) return
        started = true
        finished = false
        mutate(QuizMutation.Loading)
        launch {
            val ready = runCatching { repo.ensurePool() }.getOrDefault(false)
            if (!ready) {
                mutate(QuizMutation.NotEnoughData)
                return@launch
            }
            val questions = generator.generateSession(10)
            if (questions.isEmpty()) {
                mutate(QuizMutation.NotEnoughData)
                return@launch
            }
            mutate(QuizMutation.Started(questions))
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                onIntent(QuizIntent.TimerTick)
            }
        }
    }

    private fun tick() {
        if (state.value.phase != QuizState.Phase.Playing) return
        mutate(QuizMutation.Tick)
        if (state.value.remainingSeconds <= 0) finish()
    }

    private fun answer(optionIndex: Int) {
        val s = state.value
        if (s.phase != QuizState.Phase.Playing || s.revealed) return
        val q = s.current ?: return
        mutate(QuizMutation.Reveal(optionIndex, wasCorrect = optionIndex == q.correctIndex))
        // Brief reveal, then advance (or finish on the last question).
        launch {
            delay(700)
            if (state.value.phase != QuizState.Phase.Playing) return@launch
            if (state.value.index >= state.value.questions.lastIndex) finish()
            else mutate(QuizMutation.Advance)
        }
    }

    private fun finish() {
        if (finished) return
        finished = true
        timerJob?.cancel()
        val s = state.value
        val score = QuizScoring.score(s.correctCount, s.remainingSeconds)
        val correct = s.correctCount
        val wrong = s.questions.size - correct
        val used = QuizScoring.usedSeconds(s.remainingSeconds)
        mutate(QuizMutation.Finished)
        launch { repo.recordSession(score) }   // local stats + leaderboard submit
        sendEffect(QuizEffect.NavigateToResult(score, correct, wrong, used))
    }

    // Abandon: cancel the timer, DO NOT record the session, and go back.
    private fun abandon() {
        timerJob?.cancel()
        mutate(QuizMutation.HideAbandon)
        sendEffect(QuizEffect.NavigateBack)
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
