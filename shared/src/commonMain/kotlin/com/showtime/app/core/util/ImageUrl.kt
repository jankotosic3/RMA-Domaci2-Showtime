package com.showtime.app.core.util

object ImageUrl {
    private const val BASE = "https://image.tmdb.org/t/p/"

    // Sizes: list posters w185, detail poster w342, backdrops w780, profiles w185.
    fun of(path: String?, size: String): String? = path?.let { "$BASE$size$it" }
}
