package com.showtime.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun platformEngineClient(): HttpClient = HttpClient(OkHttp)
