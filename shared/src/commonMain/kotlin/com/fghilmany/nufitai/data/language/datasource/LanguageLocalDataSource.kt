package com.fghilmany.nufitai.data.language.datasource

import com.russhwolf.settings.Settings

private const val KEY_LANGUAGE_TAG = "language_app_tag"
private const val DEFAULT_LANGUAGE_TAG = "id"

/** Mirrors `EntitlementLocalDataSource`'s single-value `Settings` pattern. */
class LanguageLocalDataSource(private val settings: Settings) {
    fun getLanguageTag(): String = settings.getString(KEY_LANGUAGE_TAG, DEFAULT_LANGUAGE_TAG)
    fun setLanguageTag(tag: String) = settings.putString(KEY_LANGUAGE_TAG, tag)
}
