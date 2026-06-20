package com.showtime.app.feature.quiz

import com.showtime.app.core.util.ImageUrl
import com.showtime.app.data.local.db.ShowtimeDatabase
import com.showtime.app.data.local.entity.MovieEntity
import kotlin.random.Random

// Pure-ish, deterministic-given-a-seed question generator (the −2 "randomization" category).
// Pass a seeded Random in tests to make sessions reproducible.
class QuizGenerator(
    private val db: ShowtimeDatabase,
    private val random: Random = Random.Default
) {
    companion object {
        private const val MAX_PER_TYPE = 4   // cap each type per session for variety
    }

    suspend fun generateSession(count: Int = 10): List<QuizQuestion> {
        val pool = db.movieDao().moviesWithImages().shuffled(random)
        if (pool.isEmpty()) return emptyList()

        val used = mutableSetOf<String>()         // movie ids already turned into a question
        val usedImages = mutableSetOf<String>()   // image paths already shown (no repeats)
        val typeCounts = mutableMapOf<QuizType, Int>()
        val questions = mutableListOf<QuizQuestion>()

        var i = 0
        var guard = 0
        // The guard guarantees termination even when many movies fail their picked type.
        while (questions.size < count && guard < pool.size * 5) {
            guard++
            val movie = pool[i % pool.size]; i++
            if (movie.imdbId in used) continue

            val type = pickType(typeCounts) ?: continue
            val q = when (type) {
                QuizType.GUESS_MOVIE -> makeGuessMovie(movie, pool, usedImages)
                QuizType.GUESS_YEAR -> makeGuessYear(movie)
                QuizType.GUESS_LEAD_ACTOR -> makeGuessActor(movie)
            } ?: continue   // movie didn't satisfy this type → skip, try another

            questions += q
            used += movie.imdbId
            typeCounts[type] = (typeCounts[type] ?: 0) + 1
        }
        return questions
    }

    private fun pickType(counts: Map<QuizType, Int>): QuizType? {
        val available = QuizType.entries.filter { (counts[it] ?: 0) < MAX_PER_TYPE }
        return available.randomOrNull(random)
    }

    // GUESS_MOVIE: hide the title, show one random poster/backdrop; 3 wrong movie titles.
    private suspend fun makeGuessMovie(
        movie: MovieEntity,
        pool: List<MovieEntity>,
        usedImages: MutableSet<String>
    ): QuizQuestion? {
        val images = db.castImageDao().imagesFor(movie.imdbId).filter { it.filePath !in usedImages }
        val img = images.randomOrNull(random) ?: return null
        val wrongTitles = pool.asSequence()
            .filter { it.imdbId != movie.imdbId && it.title != movie.title }
            .map { it.title }
            .distinct()
            .toList()
            .shuffled(random)
            .take(3)
        if (wrongTitles.size < 3) return null

        usedImages += img.filePath
        val options = (wrongTitles + movie.title).shuffled(random)
        return QuizQuestion(
            type = QuizType.GUESS_MOVIE,
            movieId = movie.imdbId,
            promptImageUrl = ImageUrl.of(img.filePath, "w780"),
            promptTitle = null,
            options = options,
            correctIndex = options.indexOf(movie.title)
        )
    }

    // GUESS_YEAR: correct = release year; wrongs = distinct offsets ±1..±10, never the correct year.
    private fun makeGuessYear(movie: MovieEntity): QuizQuestion? {
        val year = movie.year ?: return null
        val offsets = ((-10..-1) + (1..10)).shuffled(random)
        val wrongs = mutableSetOf<Int>()
        for (o in offsets) {
            if (wrongs.size == 3) break
            val y = year + o
            if (y != year) wrongs += y
        }
        if (wrongs.size < 3) return null

        val options = (wrongs.map { it.toString() } + year.toString()).shuffled(random)
        return QuizQuestion(
            type = QuizType.GUESS_YEAR,
            movieId = movie.imdbId,
            promptImageUrl = ImageUrl.of(movie.posterPath, "w342"),
            promptTitle = movie.title,
            options = options,
            correctIndex = options.indexOf(year.toString())
        )
    }

    // GUESS_LEAD_ACTOR: correct = one of the first 3 cast; wrongs = 3 actors NOT in this movie.
    private suspend fun makeGuessActor(movie: MovieEntity): QuizQuestion? {
        val cast = db.castImageDao().castFor(movie.imdbId)
        if (cast.isEmpty()) return null
        val correct = cast.take(3).randomOrNull(random)?.name ?: return null
        // Fetch a few extra in case the correct actor also appears in other movies, then exclude them.
        val wrongs = db.castImageDao().randomOtherActors(movie.imdbId, 6)
            .filter { it != correct }
            .distinct()
        if (wrongs.size < 3) return null

        val options = (wrongs.take(3) + correct).shuffled(random)
        return QuizQuestion(
            type = QuizType.GUESS_LEAD_ACTOR,
            movieId = movie.imdbId,
            promptImageUrl = ImageUrl.of(movie.posterPath, "w342"),
            promptTitle = movie.title,
            options = options,
            correctIndex = options.indexOf(correct)
        )
    }
}
