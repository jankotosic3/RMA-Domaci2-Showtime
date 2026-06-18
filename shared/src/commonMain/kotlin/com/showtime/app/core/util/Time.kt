package com.showtime.app.core.util

// Wall-clock epoch millis. Used for ordering favorites/watchlist by insertion time.
// expect/actual instead of kotlinx-datetime to avoid that library's 0.6→0.7 Clock API churn.
expect fun nowEpochMillis(): Long
