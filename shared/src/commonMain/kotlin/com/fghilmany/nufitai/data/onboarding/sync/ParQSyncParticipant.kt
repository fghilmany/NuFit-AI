package com.fghilmany.nufitai.data.onboarding.sync

import com.fghilmany.nufitai.data.onboarding.datasource.OnboardingLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Par_q_result as ParQResultRow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.time.Instant

/**
 * Mirrors FullAssessmentSyncParticipant's worked-example pattern. Unlike that one, this also
 * rebuilds `par_q_answer_detail` (the per-question answers) from the row's `answers_json` after
 * every pull -- that child table has no sync participant of its own (ParQResult.sq).
 */
class ParQSyncParticipant(
    private val local: OnboardingLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "par_q_result"
    private val json = Json

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getParQChangedSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        rows.forEach { row ->
            val id = row.getValue("id").jsonPrimitive.content
            val remoteUpdatedAt = row.getValue("updated_at").jsonPrimitive.long
            val existing = local.getParQResultById(id)
            if (existing == null || existing.updated_at < remoteUpdatedAt) {
                val newRow = row.toRow()
                val answers = json.decodeFromString<Map<String, Boolean>>(newRow.answers_json).map { it.key to it.value }
                local.upsertParQFromRemote(newRow, answers)
            }
        }
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillParQUserId(userId)
    }

    private fun ParQResultRow.toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("answered_at", JsonPrimitive(answered_at))
        put("requires_doctor_consult", JsonPrimitive(requires_doctor_consult))
        put("consult_acknowledged_at", JsonPrimitive(consult_acknowledged_at))
        put("answers_json", JsonPrimitive(answers_json))
        put("user_id", JsonPrimitive(user_id))
        put("updated_at", JsonPrimitive(updated_at))
    }

    private fun JsonObject.toRow(): ParQResultRow = ParQResultRow(
        id = getValue("id").jsonPrimitive.content,
        answered_at = getValue("answered_at").jsonPrimitive.long,
        requires_doctor_consult = getValue("requires_doctor_consult").jsonPrimitive.long,
        consult_acknowledged_at = get("consult_acknowledged_at")?.jsonPrimitive?.long,
        answers_json = get("answers_json")?.jsonPrimitive?.contentOrNull ?: "{}",
        user_id = get("user_id")?.jsonPrimitive?.contentOrNull,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
