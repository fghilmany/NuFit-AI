package com.fghilmany.nufitai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fghilmany.nufitai.core.db.DatabaseDriverFactory
import com.fghilmany.nufitai.core.di.initKoin

private var koinStarted = false

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (!koinStarted) {
            initKoin(driverFactory = DatabaseDriverFactory(applicationContext))
            koinStarted = true
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
