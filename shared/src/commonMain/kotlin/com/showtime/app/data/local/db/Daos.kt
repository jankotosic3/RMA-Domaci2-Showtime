package com.showtime.app.data.local.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.showtime.app.data.local.entity.CastEntity
import com.showtime.app.data.local.entity.FavoriteEntity
import com.showtime.app.data.local.entity.MovieEntity
import com.showtime.app.data.local.entity.MovieImageEntity
import com.showtime.app.data.local.entity.MovieRemoteKeyEntity
import com.showtime.app.data.local.entity.QuizStatsEntity
import com.showtime.app.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Upsert suspend fun upsertAll(movies: List<MovieEntity>)
    @Upsert suspend fun upsert(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE imdbId = :id")
    fun observeMovie(id: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE imdbId = :id")
    suspend fun getMovie(id: String): MovieEntity?

    // Paging 3 source backed by Room — filtered query built dynamically
    @Query("""
        SELECT * FROM movies
        WHERE (:query IS NULL OR title LIKE '%' || :query || '%')
          AND (:genreId IS NULL OR genresCsv LIKE '%' || :genreId || ':%')
          AND (:minYear IS NULL OR year >= :minYear)
          AND (:maxYear IS NULL OR year <= :maxYear)
          AND (:minRating IS NULL OR imdbRating >= :minRating)
        ORDER BY
          CASE WHEN :sortBy = 'year'        THEN year       END DESC,
          CASE WHEN :sortBy = 'title'       THEN title      END ASC,
          CASE WHEN :sortBy = 'imdb_rating' THEN imdbRating END DESC,
          imdbVotes DESC
    """)
    fun pagingSource(
        query: String?, genreId: Int?, minYear: Int?, maxYear: Int?,
        minRating: Float?, sortBy: String?
    ): PagingSource<Int, MovieEntity>

    @Query("SELECT COUNT(*) FROM movies") suspend fun count(): Int

    @Query("SELECT * FROM movies WHERE (SELECT COUNT(*) FROM movie_images WHERE movieId = movies.imdbId) > 0")
    suspend fun moviesWithImages(): List<MovieEntity>
}

@Dao
interface RemoteKeyDao {
    @Upsert suspend fun upsertAll(keys: List<MovieRemoteKeyEntity>)
    @Query("SELECT * FROM movie_remote_keys WHERE imdbId = :id") suspend fun keyFor(id: String): MovieRemoteKeyEntity?
    @Query("DELETE FROM movie_remote_keys") suspend fun clear()
}

@Dao
interface LibraryDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC") fun observeFavoriteIds(): Flow<List<FavoriteEntity>>
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC") fun observeWatchlistIds(): Flow<List<WatchlistEntity>>
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE imdbId = :id)") fun isFavorite(id: String): Flow<Boolean>
    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE imdbId = :id)") fun isWatchlisted(id: String): Flow<Boolean>
    @Query("SELECT COUNT(*) FROM favorites") fun favoritesCount(): Flow<Int>
    @Query("SELECT COUNT(*) FROM watchlist") fun watchlistCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun addFavorite(e: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE imdbId = :id") suspend fun removeFavorite(id: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun addWatchlist(e: WatchlistEntity)
    @Query("DELETE FROM watchlist WHERE imdbId = :id") suspend fun removeWatchlist(id: String)

    @Query("DELETE FROM favorites") suspend fun clearFavorites()
    @Query("DELETE FROM watchlist") suspend fun clearWatchlist()

    // Joined views for the Favorites/Watchlist screens
    @Query("SELECT m.* FROM movies m INNER JOIN favorites f ON m.imdbId = f.imdbId ORDER BY f.addedAt DESC")
    fun observeFavoriteMovies(): Flow<List<MovieEntity>>
    @Query("SELECT m.* FROM movies m INNER JOIN watchlist w ON m.imdbId = w.imdbId ORDER BY w.addedAt DESC")
    fun observeWatchlistMovies(): Flow<List<MovieEntity>>
}

@Dao
interface CastImageDao {
    @Upsert suspend fun upsertCast(cast: List<CastEntity>)
    @Query("SELECT * FROM cast WHERE movieId = :id ORDER BY ordering ASC") suspend fun castFor(id: String): List<CastEntity>
    @Query("SELECT * FROM cast WHERE movieId = :id ORDER BY ordering ASC") fun observeCast(id: String): Flow<List<CastEntity>>
    @Upsert suspend fun upsertImages(images: List<MovieImageEntity>)
    @Query("SELECT * FROM movie_images WHERE movieId = :id") suspend fun imagesFor(id: String): List<MovieImageEntity>
    // For quiz wrong-answer pools
    @Query("SELECT DISTINCT name FROM cast WHERE movieId != :excludeMovieId ORDER BY RANDOM() LIMIT :n")
    suspend fun randomOtherActors(excludeMovieId: String, n: Int): List<String>
}

@Dao
interface QuizStatsDao {
    @Query("SELECT * FROM quiz_stats WHERE category = 1") fun observe(): Flow<QuizStatsEntity?>
    @Query("SELECT * FROM quiz_stats WHERE category = 1") suspend fun get(): QuizStatsEntity?
    @Upsert suspend fun upsert(stats: QuizStatsEntity)
    @Query("DELETE FROM quiz_stats") suspend fun clear()
}
