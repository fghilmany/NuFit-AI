package com.fghilmany.nufitai.data.monthlyplan.sync

import com.fghilmany.nufitai.data.monthlyplan.datasource.MonthlyPlanLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Plan_day_session_log as PlanDaySessionLogRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlin.time.Instant

/**
 * ADR-008 §3 follow-up. The table was already sync-ready (chat-ai.md §7 added user_id/updated_at
 * up front) but no participant ever read/wrote it remotely -- RPE/completed/skipped history was
 * silently local-only. Must be registered AFTER [PlanDaySyncParticipant] -- local FK to
 * plan_day(id).
 */
class PlanDaySessionLogSyncParticipant(
    private val local: MonthlyPlanLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "plan_day_session_log"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getSessionLogChangedSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        rows.forEach { row ->
            val id = row.getValue("id").jsonPrimitive.content
            val remoteUpdatedAt = row.getValue("updated_at").jsonPrimitive.long
            val existing = local.getSessionLogById(id)
            if (existing == null || existing.updated_at < remoteUpdatedAt) {
                local.upsertSessionLogFromRemote(row.toRow())
            }
        }
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillSessionLogUserId(userId)
    }

    private fun PlanDaySessionLogRow.toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("plan_day_id", JsonPrimitive(plan_day_id))
        put("completed_at", JsonPrimitive(completed_at))
        put("skipped_at", JsonPrimitive(skipped_at))
        put("rpe_reported", JsonPrimitive(rpe_reported))
        put("pain_reported", JsonPrimitive(pain_reported))
        put("user_id", JsonPrimitive(user_id))
        put("updated_at", JsonPrimitive(updated_at))
    }

    private fun JsonObject.toRow(): PlanDaySessionLogRow = PlanDaySessionLogRow(
        id = getValue("id").jsonPrimitive.content,
        plan_day_id = getValue("plan_day_id").jsonPrimitive.content,
        completed_at = get("completed_at")?.jsonPrimitive?.longOrNull,
        skipped_at = get("skipped_at")?.jsonPrimitive?.longOrNull,
        rpe_reported = get("rpe_reported")?.jsonPrimitive?.longOrNull,
        pain_reported = get("pain_reported")?.jsonPrimitive?.contentOrNull,
        user_id = get("user_id")?.jsonPrimitive?.contentOrNull,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
