package com.showtime.app.data.local.db

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

// Finalizes the platform builder with the BUNDLED SQLite driver so the exact same
// Room code runs on Android AND Desktop (the −5 multiplatform insurance).
fun buildDatabase(): ShowtimeDatabase =
    databaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
