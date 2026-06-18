package com.showtime.app.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import com.showtime.app.appContext

actual fun databaseBuilder(): RoomDatabase.Builder<ShowtimeDatabase> {
    val ctx = appContext
    val dbFile = ctx.getDatabasePath("showtime.db")
    return Room.databaseBuilder<ShowtimeDatabase>(ctx, dbFile.absolutePath)
}
