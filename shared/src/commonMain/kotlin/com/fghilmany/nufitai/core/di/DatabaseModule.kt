package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.core.db.DatabaseDriverFactory
import com.fghilmany.nufitai.db.NuFitDatabase
import org.koin.dsl.module

/**
 * Not scanned by koin-annotations: DatabaseDriverFactory is an expect/actual class
 * constructed differently per platform (Android needs a Context, iOS doesn't), so its
 * instance is built by each platform's entry point and handed in here explicitly.
 */
fun databaseModule(driverFactory: DatabaseDriverFactory) = module {
    single { NuFitDatabase(driver = driverFactory.createDriver()) }
}
