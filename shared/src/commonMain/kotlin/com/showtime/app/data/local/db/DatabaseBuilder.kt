package com.showtime.app.data.local.db

import androidx.room.RoomDatabase

expect fun databaseBuilder(): RoomDatabase.Builder<ShowtimeDatabase>
