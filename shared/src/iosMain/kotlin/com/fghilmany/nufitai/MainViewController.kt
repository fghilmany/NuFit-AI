package com.fghilmany.nufitai

import androidx.compose.ui.window.ComposeUIViewController
import com.fghilmany.nufitai.db.DatabaseDriverFactory
import com.fghilmany.nufitai.di.initKoin

private var koinStarted = false

fun MainViewController() = ComposeUIViewController {
    if (!koinStarted) {
        initKoin(driverFactory = DatabaseDriverFactory())
        koinStarted = true
    }
    App()
}
