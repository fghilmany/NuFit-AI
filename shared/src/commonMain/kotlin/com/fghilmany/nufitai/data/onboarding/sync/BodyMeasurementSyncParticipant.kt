package com.fghilmany.nufitai.data.onboarding.sync

import com.fghilmany.nufitai.data.onboarding.datasource.OnboardingLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Body_measurement as BodyMeasurementRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.time.Instant

/** Mirrors FullAssessmentSyncParticipant's worked-example pattern. */
class BodyMeasurementSyncParticipant(
    private val local: OnboardingLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "body_measurement"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getBodyMeasurementChangedSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        rows.forEach { row ->
            val id = row.getValue("id").jsonPrimitive.content
            val remoteUpdatedAt = row.getValue("updated_at").jsonPrimitive.long
            val existing = local.getBodyMeasurementById(id)
            if (existing == null || existing.updated_at < remoteUpdatedAt) {
                local.upsertBodyMeasurementFromRemote(row.toRow())
            }
        }
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillBodyMeasurementUserId(userId)
    }

    private fun BodyMeasurementRow.toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("recorded_at", JsonPrimitive(recorded_at))
        put("height_cm", JsonPrimitive(height_cm))
        put("weight_kg", JsonPrimitive(weight_kg))
        put("user_id", JsonPrimitive(user_id))
        put("updated_at", JsonPrimitive(updated_at))
    }

    private fun JsonObject.toRow(): BodyMeasurementRow = BodyMeasurementRow(
        id = getValue("id").jsonPrimitive.content,
        recorded_at = getValue("recorded_at").jsonPrimitive.long,
        height_cm = get("height_cm")?.jsonPrimitive?.doubleOrNull,
        weight_kg = get("weight_kg")?.jsonPrimitive?.doubleOrNull,
        user_id = get("user_id")?.jsonPrimitive?.contentOrNull,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
