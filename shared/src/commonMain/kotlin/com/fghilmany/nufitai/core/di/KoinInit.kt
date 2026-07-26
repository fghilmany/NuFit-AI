package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.core.db.DatabaseDriverFactory
import com.fghilmany.nufitai.core.settings.SettingsFactory
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    driverFactory: DatabaseDriverFactory,
    settingsFactory: SettingsFactory,
    appDeclaration: KoinAppDeclaration = {},
) {
    startKoin {
        appDeclaration()
        modules(
            databaseModule(driverFactory),
            onboardingModule,
            exerciseLibraryModule,
            fullAssessmentModule,
            monthlyPlanModule,
            assessmentDetailModule,
            ptModeModule,
            entitlementModule(settingsFactory),
            aiCustomPlanModule,
            planManagementModule,
        )
    }
}
