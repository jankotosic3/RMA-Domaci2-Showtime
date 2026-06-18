package com.showtime.app.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.showtime.app.data.local.entity.CastEntity
import com.showtime.app.data.local.entity.FavoriteEntity
import com.showtime.app.data.local.entity.MovieEntity
import com.showtime.app.data.local.entity.MovieImageEntity
import com.showtime.app.data.local.entity.MovieRemoteKeyEntity
import com.showtime.app.data.local.entity.QuizStatsEntity
import com.showtime.app.data.local.entity.WatchlistEntity

@Database(
    entities = [
        MovieEntity::class, FavoriteEntity::class, WatchlistEntity::class,
        CastEntity::class, MovieImageEntity::class, QuizStatsEntity::class,
        MovieRemoteKeyEntity::class
    ],
    version = 1
)
@ConstructedBy(ShowtimeDatabaseCtor::class)
abstract class ShowtimeDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun libraryDao(): LibraryDao
    abstract fun castImageDao(): CastImageDao
    abstract fun quizStatsDao(): QuizStatsDao
}

// Room KMP needs this constructor expect for the generated implementation.
// @ConstructedBy above links the database to it; Room generates the platform actuals.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpectFunction")
expect object ShowtimeDatabaseCtor : RoomDatabaseConstructor<ShowtimeDatabase> {
    override fun initialize(): ShowtimeDatabase
}
