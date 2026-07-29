package com.fghilmany.nufitai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fghilmany.nufitai.core.auth.CurrentActivityHolder
import com.fghilmany.nufitai.core.db.DatabaseDriverFactory
import com.fghilmany.nufitai.core.di.initKoin
import com.fghilmany.nufitai.core.security.TokenStorage
import com.fghilmany.nufitai.core.settings.SettingsFactory

private var koinStarted = false

// P-15 "Ganti Bahasa" -- base class migrated from ComponentActivity to AppCompatActivity so
// AppCompatDelegate.setApplicationLocales (applied inside initKoin) can auto-recreate this
// Activity on minSdk 31-32 (native OS-level per-app language is API 33+ only).
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (!koinStarted) {
            initKoin(
                driverFactory = DatabaseDriverFactory(applicationContext),
                settingsFactory = SettingsFactory(applicationContext),
                tokenStorage = TokenStorage(applicationContext),
            )
            koinStarted = true
        }

        setContent {
            App()
        }
    }

    // issue #83 follow-up (ADR-006) -- Credential Manager needs a live Activity at call time.
    override fun onResume() {
        super.onResume()
        CurrentActivityHolder.activity = this
    }

    override fun onPause() {
        CurrentActivityHolder.activity = null
        super.onPause()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
