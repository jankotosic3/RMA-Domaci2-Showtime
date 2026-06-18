package com.showtime.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform