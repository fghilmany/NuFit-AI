package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.profilesettings.repository.ProfileSettingsRepository

class FakeProfileSettingsRepository : ProfileSettingsRepository {
    var hasSeenMedicalDisclaimerValue: Boolean = false
    var deleteAccountResult: AppResult<Unit> = AppResult.Error(Failure.Network("no fake result configured"))
    var markMedicalDisclaimerSeenCallCount = 0
        private set
    var deleteAccountCallCount = 0
        private set

    override suspend fun hasSeenMedicalDisclaimer(): AppResult<Boolean> = AppResult.Success(hasSeenMedicalDisclaimerValue)

    override suspend fun markMedicalDisclaimerSeen(): AppResult<Unit> {
        markMedicalDisclaimerSeenCallCount++
        hasSeenMedicalDisclaimerValue = true
        return AppResult.Success(Unit)
    }

    override suspend fun deleteAccount(): AppResult<Unit> {
        deleteAccountCallCount++
        return deleteAccountResult
    }
}
