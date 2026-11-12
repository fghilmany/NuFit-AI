package com.fghilmany.nufitai.usecase.profilesettings

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.profilesettings.repository.ProfileSettingsRepository
import com.fghilmany.nufitai.usecase.auth.SignOut

/**
 * techspec profile-settings.md §7/§9 item 8 -- on success, signs out locally via the existing
 * [SignOut] (clears session, flips `isLoggedIn` false) so the device returns to Guest. [SignOut]
 * also wipes local on-device data (dev decision 2026-11-12, privacy: the account is gone, so its
 * data shouldn't linger for whoever uses this device next) -- there's nothing left to sync by
 * definition (the account itself was just deleted), so this always runs [SignOut] with
 * `force = true`. The confirmation dialog copy must state this explicitly (see DeleteAccountConfirmDialog).
 */
class DeleteAccount(
    private val repository: ProfileSettingsRepository,
    private val signOut: SignOut,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val result = repository.deleteAccount()
        if (result is AppResult.Success) {
            signOut(force = true)
        }
        return result
    }
}
