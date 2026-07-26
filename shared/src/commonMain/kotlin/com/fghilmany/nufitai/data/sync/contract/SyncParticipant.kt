package com.fghilmany.nufitai.data.sync.contract

import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * techspec §7 (auth-sync.md, issue #83) -- one adapter per sync aggregate. `SyncRepositoryImpl`
 * holds a `List<SyncParticipant>` assembled by hand in `SyncModule.kt` (manual Koin DSL has no
 * auto-multibinding). Rows travel as generic [JsonObject] so the orchestrator stays uniform
 * across aggregates with completely different shapes.
 */
interface SyncParticipant {
    val tableName: String

    suspend fun getChangedSince(since: Instant): List<JsonObject>
    suspend fun upsertRemoteRows(rows: List<JsonObject>)

    /** One-time, run once right after first successful login (techspec §7). */
    suspend fun backfillUserId(userId: String)
}
