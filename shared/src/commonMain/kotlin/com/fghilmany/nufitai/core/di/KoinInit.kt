package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.core.db.DatabaseDriverFactory
import com.fghilmany.nufitai.core.security.TokenStorage
import com.fghilmany.nufitai.core.settings.SettingsFactory
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    driverFactory: DatabaseDriverFactory,
    settingsFactory: SettingsFactory,
    tokenStorage: TokenStorage,
    appDeclaration: KoinAppDeclaration = {},
) {
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
            syncModule,
        )
    }
}
