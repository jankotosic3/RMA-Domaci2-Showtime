package com.showtime.app.feature.quiz

// Scoring formula from the spec:
//   UBP = BTO * (9 + PVT/MVT), capped at 100.00
// where BTO = number of correct answers, PVT = remaining seconds, MVT = max time (60).
// Sanity: 10 correct @60s left -> 10*(9+1)=100 ; 10 @30s -> 95 ; 8 @40s -> 77.33.
object QuizScoring {
    const val MAX_TIME = 60

    fun score(correct: Int, remainingSeconds: Int): Float {
        val raw = correct * (9f + remainingSeconds.toFloat() / MAX_TIME)
        return minOf(raw, 100f)
    }

    fun usedSeconds(remaining: Int): Int = MAX_TIME - remaining
}
