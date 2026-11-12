package com.fghilmany.nufitai.data.sync.datasource

import com.russhwolf.settings.Settings

private const val KEY_LAST_SYNCED_AT = "sync_last_synced_at"

/** techspec §7 -- one global cursor (coarse-grained, documented simplification), not a DB table. */
class SyncCursorLocalDataSource(private val settings: Settings) {
    fun getLastSyncedAtEpochMillis(): Long = settings.getLong(KEY_LAST_SYNCED_AT, 0L)
    fun setLastSyncedAtEpochMillis(epochMillis: Long) {
        settings.putLong(KEY_LAST_SYNCED_AT, epochMillis)
    }

    /** Called when local data is wiped (sign-out, or discarding local data on a sign-in
     * conflict) -- without this, the next sync would ask the server for changes "since" a
     * stale timestamp and could miss rows that haven't changed since then. */
    fun reset() {
        settings.remove(KEY_LAST_SYNCED_AT)
    }
}
