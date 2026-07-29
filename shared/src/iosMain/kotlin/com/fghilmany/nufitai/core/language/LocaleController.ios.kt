package com.fghilmany.nufitai.core.language

import com.fghilmany.nufitai.domain.language.entity.AppLanguage
import platform.Foundation.NSUserDefaults

/**
 * TODO(dev): iOS has no runtime per-app-language switch equivalent to Android's
 * AppCompatDelegate -- setting `AppleLanguages` only takes effect after the app process
 * restarts (bundle-localized resources are resolved once per process launch). This persists the
 * preference so it's picked up on next launch; prompting the user to relaunch (or calling
 * `exit(0)` yourself) is a product decision, not made here.
 */
actual class LocaleController actual constructor() {
    actual fun applyLocale(language: AppLanguage) {
        NSUserDefaults.standardUserDefaults.setObject(listOf(language.tag), forKey = "AppleLanguages")
    }
}
