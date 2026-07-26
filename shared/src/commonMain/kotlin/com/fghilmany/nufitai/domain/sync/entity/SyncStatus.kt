package com.fghilmany.nufitai.domain.sync.entity

import com.fghilmany.nufitai.core.error.Failure
import kotlin.time.Instant

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Success(val lastSyncedAt: Instant) : SyncStatus
    data class Failed(val lastAttemptAt: Instant, val failure: Failure) : SyncStatus
}
