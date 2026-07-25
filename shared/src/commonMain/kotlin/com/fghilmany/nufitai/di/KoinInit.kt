package com.fghilmany.nufitai.di

import com.fghilmany.nufitai.db.DatabaseDriverFactory
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(driverFactory: DatabaseDriverFactory, appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            databaseModule(driverFactory),
            onboardingModule,
            exerciseLibraryModule,
            fullAssessmentModule,
            monthlyPlanModule,
            assessmentDetailModule,
        )
    }
}
