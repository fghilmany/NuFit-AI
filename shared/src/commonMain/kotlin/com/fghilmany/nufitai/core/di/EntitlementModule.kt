package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.core.settings.SettingsFactory
import com.fghilmany.nufitai.data.entitlement.datasource.EntitlementLocalDataSource
import com.fghilmany.nufitai.data.entitlement.datasource.EntitlementRemoteDataSource
import com.fghilmany.nufitai.data.entitlement.repository.EntitlementRepositoryImpl
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository
import com.fghilmany.nufitai.usecase.entitlement.GetCurrentTier
import org.koin.dsl.module

/**
 * Manual Koin DSL, per ADR-001. Mirrors `databaseModule`'s pattern: [SettingsFactory] is an
 * expect/actual class built differently per platform (Android needs a Context), so its
 * instance is handed in explicitly by each platform's entry point via `initKoin`.
 */
fun entitlementModule(settingsFactory: SettingsFactory) = module {
    // issue #83 -- exposed as its own singleton so SyncModule's SyncCursorLocalDataSource can
    // reuse the same Settings instance via get(), instead of each feature creating its own.
    single { settingsFactory.createSettings() }
    single { EntitlementLocalDataSource(settings = get()) }
    single { EntitlementRemoteDataSource(httpClient = get()) }
    single<EntitlementRepository> { EntitlementRepositoryImpl(local = get(), remote = get()) }

    factory { GetCurrentTier(repository = get()) }
}
