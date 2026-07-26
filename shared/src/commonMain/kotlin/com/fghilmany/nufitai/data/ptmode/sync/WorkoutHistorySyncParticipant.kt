package com.fghilmany.nufitai.data.ptmode.sync

import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * TODO(dev): stub -- see `FullAssessmentSyncParticipant`'s doc comment for the pattern to
 * follow. This aggregate additionally bundles `workout_exercise_log`, `workout_set_log`, and
 * `plan_day_session_log` as nested children of `workout_session` (techspec §5).
 */
class WorkoutHistorySyncParticipant : SyncParticipant {
    override val tableName: String = "workout_session"
    override suspend fun getChangedSince(since: Instant): List<JsonObject> = emptyList()
    override suspend fun upsertRemoteRows(rows: List<JsonObject>) = Unit
    override suspend fun backfillUserId(userId: String) = Unit
}
