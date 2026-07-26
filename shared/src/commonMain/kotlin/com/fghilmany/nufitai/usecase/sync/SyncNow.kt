package com.fghilmany.nufitai.usecase.sync

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.sync.repository.SyncRepository

class SyncNow(private val repository: SyncRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.syncNow()
}
