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
/** [isDebugBuild] gates full header+body logging (incl. `Authorization: Bearer <token>`) --
 * never pass true for a release build, since tokens would land in device logs. */
fun networkModule(isDebugBuild: Boolean) = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            // OkHttp's own default (10s connect/read/write) is too short for the AI endpoints --
            // an LLM call, especially chat's 2-round-trip tool-calling flow, routinely takes
            // longer than that and was hitting a raw SocketTimeoutException before
            // AiCustomPlanRepositoryImpl/MealPlanRepositoryImpl's own `withTimeoutOrNull` guard
            // ever got a chance to apply (dev-reported timeout, 2026-08-01). Must always stay
            // ABOVE every repository's own GENERATE_TIMEOUT_MS, or this fires first instead
            // (exactly what happened 2026-11-10: MealPlanRepositoryImpl's guard was bumped to
            // 150_000 for the batched meal-plan-generate flow -- which can take up to ~150s -- but
            // this plugin's socketTimeoutMillis was left at the old 75_000, so it fired first
            // every time). 155_000 keeps a 5s buffer over the 150_000 app-level guard.
            install(HttpTimeout) {
                requestTimeoutMillis = 155_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 155_000
            }
            // Retrofit's HttpLoggingInterceptor equivalent -- without this, a failed request only
            // ever surfaced a bare HTTP status code, no URL/body, making every backend error a
            // guessing game (dev feedback, 2026-08-01). LogLevel.ALL (headers + body) only in
            // debug builds -- release stays LogLevel.BODY so `Authorization: Bearer <token>`
            // never lands in a release device's logs.
            install(Logging) {
                logger = object : KtorLogger {
                    private val log = AppLogger.tagged("HttpClient")
                    override fun log(message: String) = log.d { message }
                }
                level = if (isDebugBuild) LogLevel.ALL else LogLevel.BODY
            }
        }
    }
}
