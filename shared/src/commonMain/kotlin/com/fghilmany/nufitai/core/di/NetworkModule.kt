package com.fghilmany.nufitai.core.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
        }
    }
}
