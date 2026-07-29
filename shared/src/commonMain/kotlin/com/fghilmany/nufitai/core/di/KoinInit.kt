package com.fghilmany.nufitai.core.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import com.fghilmany.nufitai.core.db.DatabaseDriverFactory
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.language.LocaleController
import com.fghilmany.nufitai.core.security.TokenStorage
import com.fghilmany.nufitai.core.settings.SettingsFactory
import com.fghilmany.nufitai.domain.language.entity.AppLanguage
import com.fghilmany.nufitai.usecase.language.GetLanguage
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatform

fun initKoin(
    driverFactory: DatabaseDriverFactory,
    settingsFactory: SettingsFactory,
    tokenStorage: TokenStorage,
    appDeclaration: KoinAppDeclaration = {},
) {
    // `libs.kermit` was already a dependency but nothing ever called Logger.setLogWriters --
    // without this, AppLogger's calls never reached Logcat/console (issue reported: "susah
    // debug" with no logs showing up).
    Logger.setLogWriters(platformLogWriter())
    Logger.setMinSeverity(Severity.Debug)

    startKoin {
        appDeclaration()
        modules(
            databaseModule(driverFactory),
            networkModule,
            onboardingModule,
            exerciseLibraryModule,
            fullAssessmentModule,
            monthlyPlanModule,
            assessmentDetailModule,
            ptModeModule,
            entitlementModule(settingsFactory),
            aiCustomPlanModule,
            planManagementModule,
            authModule(tokenStorage),
            profileModule,
            profileSettingsModule,
            languageModule,
            syncModule,
        )
    }

    applyStoredLocale()
}

/**
 * P-15 "Ganti Bahasa" -- applied once at app startup, in addition to whenever the user changes
 * it (SetLanguage). Kept here (not in each platform's entry point) so MainActivity et al. don't
 * need direct access to kotlinx.coroutines/koin-core, which are `implementation`-scoped
 * dependencies of this module, not transitively visible to consumers like `androidApp`.
 */
private fun applyStoredLocale() {
    val koin = KoinPlatform.getKoin()
    val getLanguage = koin.get<GetLanguage>()
    val localeController = koin.get<LocaleController>()
    // One-off Settings read at startup, not a real network call -- blocking is negligible here.
    runBlocking {
        when (val result = getLanguage()) {
            is AppResult.Success -> localeController.applyLocale(result.data)
            is AppResult.Error -> localeController.applyLocale(AppLanguage.INDONESIAN)
        }
    }
}
