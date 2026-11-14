package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.sync.repository.SyncRepository

/** User's choice on [SignInOutcome.NoRemoteData]: keep this device's current local plan --
 * there's nothing remote to conflict with, so a normal sync just pushes it up. */
class SaveLocalDataAndSync(private val syncRepository: SyncRepository) {
    suspend operator fun invoke(): AppResult<Unit> = syncRepository.syncNow()
}
