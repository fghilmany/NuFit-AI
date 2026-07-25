package com.fghilmany.nufitai.core.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.fghilmany.nufitai.db.NuFitDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(NuFitDatabase.Schema, context, "nufit.db")
}
