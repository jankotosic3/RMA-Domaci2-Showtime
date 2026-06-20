package com.showtime.app.feature.quiz

// The three question types the generator can produce.
enum class QuizType { GUESS_MOVIE, GUESS_YEAR, GUESS_LEAD_ACTOR }

// One fully-formed question. Immutable so it can live in ViewState.
data class QuizQuestion(
    val type: QuizType,
    val movieId: String,
    val promptImageUrl: String?,    // poster/backdrop for GUESS_MOVIE; poster for the others
    val promptTitle: String?,       // null for GUESS_MOVIE (the title is what's being hidden)
    val options: List<String>,      // exactly 4 options
    val correctIndex: Int
)