package com.fghilmany.nufitai.usecase.sync

import com.fghilmany.nufitai.domain.sync.entity.SyncStatus
import com.fghilmany.nufitai.domain.sync.repository.SyncRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveSyncStatus(private val repository: SyncRepository) {
    operator fun invoke(): StateFlow<SyncStatus> = repository.status
}
