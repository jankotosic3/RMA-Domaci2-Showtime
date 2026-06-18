package com.showtime.app

import android.content.Context

// Holds the Android application Context for shared androidMain actuals (Room, DataStore).
// Set once from ShowtimeApplication.onCreate via initAppContext(this).
lateinit var appContext: Context
    private set

fun initAppContext(context: Context) {
    appContext = context.applicationContext
}
