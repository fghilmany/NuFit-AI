package com.fghilmany.nufitai.usecase.profilesettings

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.profilesettings.repository.ProfileSettingsRepository

class MarkMedicalDisclaimerSeen(private val repository: ProfileSettingsRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.markMedicalDisclaimerSeen()
}
