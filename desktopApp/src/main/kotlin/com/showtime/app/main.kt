package com.showtime.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.showtime.app.di.appModules
import org.koin.core.context.startKoin

fun main() {
    startKoin { modules(appModules()) }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Showtime",
        ) {
            App()
        }
    }
}
