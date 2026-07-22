package com.fghilmany.nufitai.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.fghilmany.nufitai.db.NuFitDatabase
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
                onboardingModule,
            )
            checkModules()
        }
    }
}
