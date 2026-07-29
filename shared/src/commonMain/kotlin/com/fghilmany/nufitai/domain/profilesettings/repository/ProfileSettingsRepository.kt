package com.fghilmany.nufitai.domain.profilesettings.repository

import com.fghilmany.nufitai.core.error.AppResult

/**
 * techspec profile-settings.md §6/§7. `deleteAccount` calls the not-yet-deployed Supabase Edge
 * Function (tracked as issue #86) -- this repository's contract is stable regardless of when
 * that lands.
 */
interface ProfileSettingsRepository {
    suspend fun hasSeenMedicalDisclaimer(): AppResult<Boolean>
    suspend fun markMedicalDisclaimerSeen(): AppResult<Unit>
    suspend fun deleteAccount(): AppResult<Unit>
}
