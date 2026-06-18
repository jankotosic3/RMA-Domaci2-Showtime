package com.showtime.app.navigation

import kotlinx.serialization.Serializable

// Auth graph
@Serializable
object AuthLanding

@Serializable
object Login

@Serializable
object Signup

// Main graph — bottom-nav top-level destinations
@Serializable
object Catalog

@Serializable
object Favorites

@Serializable
object Watchlist

@Serializable
object Quiz

@Serializable
object Profile

// Pushed on top of the main graph
@Serializable
data class Detail(val movieId: String)
