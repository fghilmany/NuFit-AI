package com.fghilmany.nufitai.core.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.fghilmany.nufitai.domain.language.entity.AppLanguage

/**
 * P-15 "Ganti Bahasa" -- AppCompatDelegate's per-app language backport. Requires the Activity
 * base class to be (or wrap) AppCompatActivity to auto-recreate on minSdk 31-32; `MainActivity`
 * was migrated from `ComponentActivity` to `AppCompatActivity` for this (see MainActivity.kt).
 * On API 33+ this also engages the OS-level per-app language store, redundantly but harmlessly.
 */
actual class LocaleController actual constructor() {
    actual fun applyLocale(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }
}
