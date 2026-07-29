package com.fghilmany.nufitai.data.profilesettings.datasource

import com.russhwolf.settings.Settings

private const val KEY_HAS_SEEN_MEDICAL_DISCLAIMER = "profile_settings_has_seen_medical_disclaimer"

/** Mirrors `EntitlementLocalDataSource`'s pattern -- a single boolean doesn't warrant a `.sq` table. */
class ProfileSettingsLocalDataSource(private val settings: Settings) {
    fun hasSeenMedicalDisclaimer(): Boolean = settings.getBoolean(KEY_HAS_SEEN_MEDICAL_DISCLAIMER, false)
    fun markMedicalDisclaimerSeen() = settings.putBoolean(KEY_HAS_SEEN_MEDICAL_DISCLAIMER, true)
}
