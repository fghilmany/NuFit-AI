package com.fghilmany.nufitai

import androidx.compose.ui.window.ComposeUIViewController
import com.fghilmany.nufitai.core.db.DatabaseDriverFactory
import com.fghilmany.nufitai.core.di.initKoin

private var koinStarted = false

fun MainViewController() = ComposeUIViewController {
    if (!koinStarted) {
        initKoin(driverFactory = DatabaseDriverFactory())
        koinStarted = true
    }
    App()
}
