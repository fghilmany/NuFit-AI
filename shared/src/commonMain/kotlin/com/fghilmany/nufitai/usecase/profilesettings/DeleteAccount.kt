package com.fghilmany.nufitai.usecase.profilesettings

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.profilesettings.repository.ProfileSettingsRepository
import com.fghilmany.nufitai.usecase.auth.SignOut

/**
 * techspec profile-settings.md §7/§9 item 8 -- on success, signs out locally via the existing
 * [SignOut] (clears session, flips `isLoggedIn` false) so the device returns to Guest. Local
 * on-device data (plans, logs, body measurements) is deliberately NOT wiped here, mirroring
 * `SignOut`'s own documented "local data stays cached" behavior -- only the cloud account/data
 * is gone. The confirmation dialog copy must state this explicitly (see DeleteAccountConfirmDialog).
 */
class DeleteAccount(
    private val repository: ProfileSettingsRepository,
    private val signOut: SignOut,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val result = repository.deleteAccount()
        if (result is AppResult.Success) {
            signOut()
        }
        return result
    }
}
