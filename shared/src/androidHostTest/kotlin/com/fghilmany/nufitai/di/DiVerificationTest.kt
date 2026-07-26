package com.fghilmany.nufitai.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.fghilmany.nufitai.core.di.aiCustomPlanModule
import com.fghilmany.nufitai.core.di.assessmentDetailModule
import com.fghilmany.nufitai.core.di.exerciseLibraryModule
import com.fghilmany.nufitai.core.di.fullAssessmentModule
import com.fghilmany.nufitai.core.di.monthlyPlanModule
import com.fghilmany.nufitai.core.di.onboardingModule
import com.fghilmany.nufitai.core.di.planManagementModule
import com.fghilmany.nufitai.core.di.ptModeModule
import com.fghilmany.nufitai.data.entitlement.datasource.EntitlementLocalDataSource
import com.fghilmany.nufitai.data.entitlement.repository.EntitlementRepositoryImpl
import com.fghilmany.nufitai.db.NuFitDatabase
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository
import com.fghilmany.nufitai.usecase.entitlement.GetCurrentTier
import com.fghilmany.nufitai.usecase.entitlement.SetDebugEntitlement
import com.russhwolf.settings.MapSettings
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.check.checkModules
import kotlin.test.AfterTest
import kotlin.test.Test

class DiVerificationTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun verifyKoinConfiguration() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        NuFitDatabase.Schema.create(driver)

        startKoin {
            modules(
                module { single { NuFitDatabase(driver = driver) } },
                // issue #81 -- SettingsFactory needs an Android Context this JVM host test doesn't
                // have; mirrors how databaseModule(driverFactory) is bypassed above with a direct
                // NuFitDatabase binding instead.
                module {
                    single { EntitlementLocalDataSource(settings = MapSettings()) }
                    single<EntitlementRepository> { EntitlementRepositoryImpl(local = get()) }
                    factory { GetCurrentTier(repository = get()) }
                    factory { SetDebugEntitlement(repository = get()) }
                },
                onboardingModule,
                exerciseLibraryModule,
                fullAssessmentModule,
                monthlyPlanModule,
                assessmentDetailModule,
                ptModeModule,
                aiCustomPlanModule,
                planManagementModule,
            )
            checkModules()
        }
    }
}
