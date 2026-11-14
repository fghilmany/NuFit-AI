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

    /** PostgREST upsert conflict target (`?on_conflict=`) -- every synced table's primary key is
     * `id` except `user_profile`, whose PK is `user_id` (no synthetic id column). */
    val conflictColumn: String get() = "id"

    suspend fun getChangedSince(since: Instant): List<JsonObject>
    suspend fun upsertRemoteRows(rows: List<JsonObject>)

    /** One-time, run once right after first successful login (techspec §7). */
    suspend fun backfillUserId(userId: String)
}
