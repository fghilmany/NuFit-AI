package com.fghilmany.nufitai.usecase.profilesettings

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.profilesettings.repository.ProfileSettingsRepository

/** Used by Splash routing (techspec profile-settings.md §7) -- takes priority over the ParQ/Home branch. */
class HasSeenMedicalDisclaimer(private val repository: ProfileSettingsRepository) {
    suspend operator fun invoke(): AppResult<Boolean> = repository.hasSeenMedicalDisclaimer()
}
