package com.showtime.app

import com.showtime.app.data.local.db.buildDatabase
import com.showtime.app.data.local.entity.QuizStatsEntity
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

// Proves the Desktop/JVM Room target actually OPENS at runtime: the bundled
// SQLite native driver loads and a write→read round-trips through the schema.
class DatabaseOpenDesktopTest {

    @Test
    fun opensAndPersistsQuizStats() = runBlocking {
        val db = buildDatabase()
        try {
            db.quizStatsDao().clear()
            db.quizStatsDao().upsert(QuizStatsEntity(category = 1, bestScore = 42f, gamesPlayed = 3))
            val read = db.quizStatsDao().get()
            assertEquals(42f, read?.bestScore)
            assertEquals(3, read?.gamesPlayed)
            db.quizStatsDao().clear()
        } finally {
            db.close()
        }
    }
}
