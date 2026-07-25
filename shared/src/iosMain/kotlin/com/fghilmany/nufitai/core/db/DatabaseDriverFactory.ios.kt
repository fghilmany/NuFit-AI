package com.fghilmany.nufitai.core.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.fghilmany.nufitai.db.NuFitDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(NuFitDatabase.Schema, "nufit.db")
}
