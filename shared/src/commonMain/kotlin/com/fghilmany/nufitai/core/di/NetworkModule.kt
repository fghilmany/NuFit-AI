package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.core.logging.AppLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * issue #83 -- extracted from `aiCustomPlanModule` (this repo's first network feature) so Auth &
 * Sync's `AuthModule`/`SyncModule` share the same `HttpClient` singleton instead of each feature
 * defining its own. Manual Koin DSL, per ADR-001.
 */
val networkModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            // OkHttp's own default (10s connect/read/write) is too short for the AI endpoints --
            // an LLM call, especially chat's 2-round-trip tool-calling flow, routinely takes
            // longer than that and was hitting a raw SocketTimeoutException before
            // AiCustomPlanRepositoryImpl/MealPlanRepositoryImpl's own 30s `withTimeoutOrNull`
            // guard ever got a chance to apply (dev-reported timeout, 2026-08-01). 45s gives that
            // app-level guard room to be the one that actually fires.
            install(HttpTimeout) {
                requestTimeoutMillis = 45_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 45_000
            }
            // Retrofit's HttpLoggingInterceptor equivalent -- without this, a failed request only
            // ever surfaced a bare HTTP status code, no URL/body, making every backend error a
            // guessing game (dev feedback, 2026-08-01). LogLevel.BODY (not ALL/HEADERS) so
            // `Authorization: Bearer <token>` never lands in device logs.
            install(Logging) {
                logger = object : KtorLogger {
                    private val log = AppLogger.tagged("HttpClient")
                    override fun log(message: String) = log.d { message }
                }
                level = LogLevel.BODY
            }
        }
    }
}
