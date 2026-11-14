package com.fghilmany.nufitai.data.monthlyplan.sync

import com.fghilmany.nufitai.data.monthlyplan.datasource.MonthlyPlanLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Planned_exercise as PlannedExerciseRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlin.time.Instant

/**
 * ADR-008 §3 follow-up -- same gap as [PlanDaySyncParticipant], one level deeper. Must be
 * registered AFTER [PlanDaySyncParticipant] in SyncModule's participant list -- local FK to
 * plan_day(id).
 */
class PlannedExerciseSyncParticipant(
    private val local: MonthlyPlanLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "planned_exercise"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getPlannedExerciseChangedSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        rows.forEach { row ->
            val id = row.getValue("id").jsonPrimitive.content
            val remoteUpdatedAt = row.getValue("updated_at").jsonPrimitive.long
            val existing = local.getPlannedExerciseById(id)
            if (existing == null || existing.updated_at < remoteUpdatedAt) {
                local.upsertPlannedExerciseFromRemote(row.toRow())
            }
        }
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillPlannedExerciseUserId(userId)
    }

    private fun PlannedExerciseRow.toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("plan_day_id", JsonPrimitive(plan_day_id))
        put("slot", JsonPrimitive(slot))
        put("exercise_id", JsonPrimitive(exercise_id))
        put("sets", JsonPrimitive(sets))
        put("rep_range_or_duration", JsonPrimitive(rep_range_or_duration))
        put("rpe_target_min", JsonPrimitive(rpe_target_min))
        put("rpe_target_max", JsonPrimitive(rpe_target_max))
        put("reason_rule_ids", JsonPrimitive(reason_rule_ids))
        put("rest_seconds", JsonPrimitive(rest_seconds))
        put("user_id", JsonPrimitive(user_id))
        put("updated_at", JsonPrimitive(updated_at))
    }

    private fun JsonObject.toRow(): PlannedExerciseRow = PlannedExerciseRow(
        id = getValue("id").jsonPrimitive.content,
        plan_day_id = getValue("plan_day_id").jsonPrimitive.content,
        slot = getValue("slot").jsonPrimitive.content,
        exercise_id = getValue("exercise_id").jsonPrimitive.content,
        sets = getValue("sets").jsonPrimitive.long,
        rep_range_or_duration = getValue("rep_range_or_duration").jsonPrimitive.content,
        rpe_target_min = getValue("rpe_target_min").jsonPrimitive.long,
        rpe_target_max = getValue("rpe_target_max").jsonPrimitive.long,
        reason_rule_ids = getValue("reason_rule_ids").jsonPrimitive.content,
        rest_seconds = get("rest_seconds")?.jsonPrimitive?.longOrNull,
        user_id = get("user_id")?.jsonPrimitive?.contentOrNull,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
