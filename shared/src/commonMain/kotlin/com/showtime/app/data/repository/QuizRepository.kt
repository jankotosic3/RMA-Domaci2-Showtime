package com.showtime.app.data.repository

import com.showtime.app.data.local.db.ShowtimeDatabase
import com.showtime.app.data.local.entity.QuizStatsEntity
import com.showtime.app.data.mapper.toCastEntity
import com.showtime.app.data.mapper.toEntity
import com.showtime.app.data.mapper.toImageEntities
import com.showtime.app.data.remote.api.MoviesApi
import com.showtime.app.data.remote.api.ShowtimeApi
import com.showtime.app.data.remote.dto.QuizResultRequest
import com.showtime.app.domain.model.QuizStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuizRepository(
    private val db: ShowtimeDatabase,
    private val moviesApi: MoviesApi,
    private val showtimeApi: ShowtimeApi
) {
    // The quiz needs ≥10 movies that each have at least one cached image. If Room is short,
    // bootstrap the top movies and fetch images + cast for enough of them.
    suspend fun ensurePool(): Boolean {
        var ready = db.movieDao().moviesWithImages().size >= 10
        if (!ready) {
            val top = moviesApi.getMovies(page = 1, pageSize = 100, sortBy = "imdb_votes", sortOrder = "desc")
            db.movieDao().upsertAll(top.items.map { it.toEntity() })
            top.items.take(30).forEach { m ->
                runCatching {
                    val imgs = moviesApi.getImages(m.imdbId)
                    db.castImageDao().upsertImages(imgs.toImageEntities(m.imdbId))
                    val cast = moviesApi.getCast(m.imdbId, pageSize = 10).items
                    db.castImageDao().upsertCast(cast.mapIndexed { i, c -> c.toCastEntity(m.imdbId, i) })
                }
            }
            ready = db.movieDao().moviesWithImages().size >= 10
        }
        return ready
    }

    // Room is the SSOT for profile stats; the UI observes this flow.
    fun observeStats(): Flow<QuizStats> =
        db.quizStatsDao().observe().map { e ->
            e?.let { QuizStats(it.bestScore, it.gamesPlayed) } ?: QuizStats(0f, 0)
        }

    // On a finished session: update local best/games-played, then submit to the global leaderboard.
    suspend fun recordSession(score: Float) {
        val current = db.quizStatsDao().get() ?: QuizStatsEntity()
        db.quizStatsDao().upsert(
            current.copy(
                bestScore = maxOf(current.bestScore, score),
                gamesPlayed = current.gamesPlayed + 1
            )
        )
        runCatching { showtimeApi.postQuizResult(QuizResultRequest(score = score, category = 1)) }
    }
}
