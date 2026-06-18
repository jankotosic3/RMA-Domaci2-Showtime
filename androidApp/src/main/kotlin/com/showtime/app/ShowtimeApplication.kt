package com.showtime.app

import android.app.Application
import com.showtime.app.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ShowtimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initAppContext(this)   // sets the Android Context for shared actuals (Room/DataStore)
        startKoin {
            androidContext(this@ShowtimeApplication)
            modules(appModules())
        }
    }
}
