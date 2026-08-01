package com.fghilmany.nufitai.data.fullassessment.sync

import com.fghilmany.nufitai.data.fullassessment.datasource.FullAssessmentLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Full_assessment_result as FullAssessmentResultRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlin.time.Instant

/**
 * techspec chat-ai.md §3/§6/§7 -- was a stub (`getChangedSince` always returned empty); real now
 * so Chat AI's server-side grounding tools have real data to read. Mirrors
 * `MonthlyPlanSyncParticipant`'s worked-example pattern exactly. Scope note: syncs the
 * `full_assessment_result` row itself only -- nested `full_assessment_flag_detail` (PAR-Q
 * category B / postural / movement flags) sync is a follow-up, same kind of scope note
 * `MonthlyPlanSyncParticipant` already carries for its own nested children.
 */
class FullAssessmentSyncParticipant(
    private val local: FullAssessmentLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "full_assessment_result"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getChangedSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        rows.forEach { row ->
            val id = row.getValue("id").jsonPrimitive.content
            val remoteUpdatedAt = row.getValue("updated_at").jsonPrimitive.long
            val existing = local.getById(id)
            if (existing == null || existing.updated_at < remoteUpdatedAt) {
                local.upsertFromRemote(row.toRow())
            }
        }
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillUserId(userId)
    }

    private fun FullAssessmentResultRow.toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("completed_at", JsonPrimitive(completed_at))
        put("age", JsonPrimitive(age))
        put("gender", JsonPrimitive(gender))
        put("par_q_answers", JsonPrimitive(par_q_answers))
        put("hard_stop_flagged", JsonPrimitive(hard_stop_flagged))
        put("hard_stop_acknowledged_at", JsonPrimitive(hard_stop_acknowledged_at))
        put("equipment_preference", JsonPrimitive(equipment_preference))
        put("injury_history", JsonPrimitive(injury_history))
        put("injury_history_note", JsonPrimitive(injury_history_note))
        put("capacity_test", JsonPrimitive(capacity_test))
        put("goal", JsonPrimitive(goal))
        put("sessions_per_week", JsonPrimitive(sessions_per_week))
        put("selected_weekdays", JsonPrimitive(selected_weekdays))
        put("session_duration_minutes", JsonPrimitive(session_duration_minutes))
        put("activity_level", JsonPrimitive(activity_level))
        put("user_id", JsonPrimitive(user_id))
        put("updated_at", JsonPrimitive(updated_at))
    }

    private fun JsonObject.toRow(): FullAssessmentResultRow = FullAssessmentResultRow(
        id = getValue("id").jsonPrimitive.content,
        completed_at = getValue("completed_at").jsonPrimitive.long,
        age = get("age")?.jsonPrimitive?.longOrNull,
        gender = get("gender")?.jsonPrimitive?.contentOrNull,
        par_q_answers = getValue("par_q_answers").jsonPrimitive.content,
        hard_stop_flagged = getValue("hard_stop_flagged").jsonPrimitive.long,
        hard_stop_acknowledged_at = get("hard_stop_acknowledged_at")?.jsonPrimitive?.longOrNull,
        equipment_preference = getValue("equipment_preference").jsonPrimitive.content,
        injury_history = getValue("injury_history").jsonPrimitive.content,
        injury_history_note = get("injury_history_note")?.jsonPrimitive?.contentOrNull,
        capacity_test = get("capacity_test")?.jsonPrimitive?.contentOrNull,
        goal = getValue("goal").jsonPrimitive.content,
        sessions_per_week = getValue("sessions_per_week").jsonPrimitive.long,
        selected_weekdays = getValue("selected_weekdays").jsonPrimitive.content,
        session_duration_minutes = getValue("session_duration_minutes").jsonPrimitive.long,
        activity_level = get("activity_level")?.jsonPrimitive?.contentOrNull,
        user_id = get("user_id")?.jsonPrimitive?.contentOrNull,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
