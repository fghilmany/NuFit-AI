package com.fghilmany.nufitai.data.ptmode.sync

import com.fghilmany.nufitai.data.ptmode.datasource.PtModeLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Workout_session as WorkoutSessionRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlin.time.Instant

/**
 * techspec chat-ai.md §3/§6/§7 -- was a stub; real now so Chat AI's server-side grounding tools
 * (`get_workout_history`) have real data to read. Mirrors `MonthlyPlanSyncParticipant`'s
 * worked-example pattern exactly. Scope note: syncs the `workout_session` row itself only --
 * nested `workout_exercise_log`/`workout_set_log`/`plan_day_session_log` sync is a follow-up,
 * same kind of scope note `MonthlyPlanSyncParticipant` already carries for its own nested
 * `plan_day`/`planned_exercise` children (a deviation from techspec chat-ai.md's literal wording
 * of "nests the three child tables' rows" -- flagged to the dev as part of this stage's summary).
 */
class WorkoutHistorySyncParticipant(
    private val local: PtModeLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "workout_session"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getChangedSessionsSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        rows.forEach { row ->
            val id = row.getValue("id").jsonPrimitive.content
            val remoteUpdatedAt = row.getValue("updated_at").jsonPrimitive.long
            val existing = local.getSessionById(id)
            if (existing == null || existing.updated_at < remoteUpdatedAt) {
                local.upsertSessionFromRemote(row.toRow())
            }
        }
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillSessionUserId(userId)
    }

    private fun WorkoutSessionRow.toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("plan_day_id", JsonPrimitive(plan_day_id))
        put("started_at", JsonPrimitive(started_at))
        put("completed_at", JsonPrimitive(completed_at))
        put("status", JsonPrimitive(status))
        put("current_exercise_index", JsonPrimitive(current_exercise_index))
        put("current_set_number", JsonPrimitive(current_set_number))
        put("rest_end_at", JsonPrimitive(rest_end_at))
        put("user_id", JsonPrimitive(user_id))
        put("updated_at", JsonPrimitive(updated_at))
    }

    private fun JsonObject.toRow(): WorkoutSessionRow = WorkoutSessionRow(
        id = getValue("id").jsonPrimitive.content,
        plan_day_id = getValue("plan_day_id").jsonPrimitive.content,
        started_at = getValue("started_at").jsonPrimitive.long,
        completed_at = get("completed_at")?.jsonPrimitive?.longOrNull,
        status = getValue("status").jsonPrimitive.content,
        current_exercise_index = getValue("current_exercise_index").jsonPrimitive.long,
        current_set_number = getValue("current_set_number").jsonPrimitive.long,
        rest_end_at = get("rest_end_at")?.jsonPrimitive?.longOrNull,
        user_id = get("user_id")?.jsonPrimitive?.contentOrNull,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
