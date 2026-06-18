package com.showtime.app.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun databaseBuilder(): RoomDatabase.Builder<ShowtimeDatabase> {
    val dbFile = File(System.getProperty("user.home"), ".showtime/showtime.db")
        .also { it.parentFile?.mkdirs() }
    return Room.databaseBuilder<ShowtimeDatabase>(dbFile.absolutePath)
}
