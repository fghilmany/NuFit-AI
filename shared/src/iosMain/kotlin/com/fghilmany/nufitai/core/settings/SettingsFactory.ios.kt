package com.fghilmany.nufitai.core.settings

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual class SettingsFactory {
    actual fun createSettings(): Settings = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
}
