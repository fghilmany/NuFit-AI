package com.fghilmany.nufitai

import androidx.compose.ui.window.ComposeUIViewController
import com.fghilmany.nufitai.core.db.DatabaseDriverFactory
import com.fghilmany.nufitai.core.di.initKoin
import com.fghilmany.nufitai.core.security.TokenStorage
import com.fghilmany.nufitai.core.settings.SettingsFactory

private var koinStarted = false

fun MainViewController() = ComposeUIViewController {
    if (!koinStarted) {
        // issue #83 -- also fixes a pre-existing bug: this call was missing settingsFactory
        // entirely (SettingsFactory has no default), so iOS didn't compile before this fix.
        initKoin(
            driverFactory = DatabaseDriverFactory(),
            settingsFactory = SettingsFactory(),
            tokenStorage = TokenStorage(),
        )
        koinStarted = true
    }
    App()
}
