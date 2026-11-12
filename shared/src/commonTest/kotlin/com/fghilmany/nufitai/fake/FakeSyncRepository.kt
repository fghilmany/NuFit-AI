package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.sync.entity.SyncStatus
import com.fghilmany.nufitai.domain.sync.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSyncRepository : SyncRepository {
    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val status: StateFlow<SyncStatus> = _status

    var syncNowCallCount = 0
        private set
    var clearAllLocalDataCallCount = 0
        private set
    var syncNowResult: AppResult<Unit> = AppResult.Success(Unit)
    var hasLocalUserDataResult: AppResult<Boolean> = AppResult.Success(false)
    var clearAllLocalDataResult: AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun syncNow(): AppResult<Unit> {
        syncNowCallCount++
        return syncNowResult
    }

    override suspend fun clearAllLocalData(): AppResult<Unit> {
        clearAllLocalDataCallCount++
        return clearAllLocalDataResult
    }

    override suspend fun hasLocalUserData(): AppResult<Boolean> = hasLocalUserDataResult
}
