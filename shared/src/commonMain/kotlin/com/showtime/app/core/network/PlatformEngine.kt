package com.showtime.app.core.network

import io.ktor.client.HttpClient

// A bare engine HttpClient per platform (OkHttp works for both Android and JVM).
// createHttpClient() reconfigures this with content negotiation, auth, and 401 handling.
expect fun platformEngineClient(): HttpClient
