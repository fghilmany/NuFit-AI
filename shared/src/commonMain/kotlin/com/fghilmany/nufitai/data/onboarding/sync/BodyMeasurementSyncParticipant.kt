package com.fghilmany.nufitai.data.onboarding.sync

import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/** TODO(dev): stub -- see `FullAssessmentSyncParticipant`'s doc comment for the pattern to follow. */
class BodyMeasurementSyncParticipant : SyncParticipant {
    override val tableName: String = "body_measurement"
    override suspend fun getChangedSince(since: Instant): List<JsonObject> = emptyList()
    override suspend fun upsertRemoteRows(rows: List<JsonObject>) = Unit
    override suspend fun backfillUserId(userId: String) = Unit
}
