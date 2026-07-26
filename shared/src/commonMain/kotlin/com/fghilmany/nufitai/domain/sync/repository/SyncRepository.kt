package com.fghilmany.nufitai.domain.sync.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.sync.entity.SyncStatus
import kotlinx.coroutines.flow.StateFlow

/** techspec §5/§7 (auth-sync.md) -- pushes local changes then pulls remote ones, LWW per record via `updated_at`. */
interface SyncRepository {
    val status: StateFlow<SyncStatus>
    suspend fun syncNow(): AppResult<Unit>
}
