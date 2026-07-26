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

    override suspend fun syncNow(): AppResult<Unit> {
        syncNowCallCount++
        return AppResult.Success(Unit)
    }
}
