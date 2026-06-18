package com.showtime.app.core.network

import com.showtime.app.data.local.datastore.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

// A simple callback the AuthRepository registers to react to a global 401.
fun interface UnauthorizedHandler { suspend fun onUnauthorized() }

fun createHttpClient(
    engineClient: HttpClient,           // platform-provided OkHttp engine wrapper
    tokenStore: TokenStore,
    onUnauthorized: UnauthorizedHandler
): HttpClient = engineClient.config {
    expectSuccess = true

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
    install(Logging) { level = LogLevel.INFO }

    defaultRequest {
        url("https://rma.finlab.rs/")
        // ktor 3.x client ContentNegotiation only serializes a @Body when the request
        // carries a matching Content-Type; Ktorfit doesn't set one, so POSTs would fail
        // with "Fail to prepare request body for sending" without this default.
        contentType(ContentType.Application.Json)
    }

    // Attach bearer token to every request that has one stored.
    install(createClientPlugin("AuthTokenPlugin") {
        onRequest { request, _ ->
            val token = tokenStore.tokenFlow.first()
            if (token != null) request.header("Authorization", "Bearer $token")
        }
    })

    // Centralized 401 → forced logout (no per-screen auth logic anywhere).
    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, _ ->
            val response = (cause as? ResponseException)?.response
            if (response?.status?.value == 401) {
                onUnauthorized.onUnauthorized()
            }
        }
    }
}
