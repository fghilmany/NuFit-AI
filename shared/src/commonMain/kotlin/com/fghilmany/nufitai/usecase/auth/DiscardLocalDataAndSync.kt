package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.sync.repository.SyncRepository

/** User's choice on [SignInOutcome.LocalDataConflict]: adopt the cloud account -- wipes this
 * device's local guest data (it doesn't belong to the account just signed into) then pulls the
 * account's real data down fresh. The push half of a normal sync is a no-op afterward since
 * there's nothing local left to push. */
class DiscardLocalDataAndSync(private val syncRepository: SyncRepository) {
    suspend operator fun invoke(): AppResult<Unit> {
        val cleared = syncRepository.clearAllLocalData()
        if (cleared is AppResult.Error) return cleared
        return syncRepository.syncNow()
    }
}
