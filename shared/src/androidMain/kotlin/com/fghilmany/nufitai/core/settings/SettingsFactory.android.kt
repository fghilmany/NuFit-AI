package com.fghilmany.nufitai.core.settings

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual class SettingsFactory(private val context: Context) {
    actual fun createSettings(): Settings =
        SharedPreferencesSettings(context.getSharedPreferences("nufit_settings", Context.MODE_PRIVATE))
}
