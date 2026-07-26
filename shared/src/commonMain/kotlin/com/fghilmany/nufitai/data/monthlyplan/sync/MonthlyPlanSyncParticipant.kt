package com.fghilmany.nufitai.data.monthlyplan.sync

import com.fghilmany.nufitai.data.monthlyplan.datasource.MonthlyPlanLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Monthly_plan as MonthlyPlanRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlin.time.Instant

/**
 * techspec §6/§7 (auth-sync.md) -- worked example [SyncParticipant]. Scope note: this pass syncs
 * the `monthly_plan` row itself (the aggregate parent); nested `plan_day`/`planned_exercise` rows
 * are not yet included in the payload -- a documented follow-up once the dev extends this same
 * shape, not a silent gap (techspec §3 sync-scope decision).
 */
class MonthlyPlanSyncParticipant(
    private val local: MonthlyPlanLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "monthly_plan"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getChangedSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        rows.forEach { row ->
            val id = row.getValue("id").jsonPrimitive.content
            val remoteUpdatedAt = row.getValue("updated_at").jsonPrimitive.long
            val existing = local.getById(id)
            // LWW per record (techspec §5/§9 item 19): only apply if remote is newer.
            if (existing == null || existing.updated_at < remoteUpdatedAt) {
                local.upsertFromRemote(row.toRow())
            }
        }
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillUserId(userId)
    }

    private fun MonthlyPlanRow.toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("started_at", JsonPrimitive(started_at))
        put("cycle_number", JsonPrimitive(cycle_number))
        put("source", JsonPrimitive(source))
        put("status", JsonPrimitive(status))
        put("level_meta", JsonPrimitive(level_meta))
        put("goal_meta", JsonPrimitive(goal_meta))
        put("smart_goal_meta", JsonPrimitive(smart_goal_meta))
        put("active_flags", JsonPrimitive(active_flags))
        put("starting_level_per_pattern", JsonPrimitive(starting_level_per_pattern))
        put("mode", JsonPrimitive(mode))
        put("checkpoint_days", JsonPrimitive(checkpoint_days))
        put("deleted_at", JsonPrimitive(deleted_at))
        put("user_id", JsonPrimitive(user_id))
        put("updated_at", JsonPrimitive(updated_at))
    }

    private fun JsonObject.toRow(): MonthlyPlanRow = MonthlyPlanRow(
        id = getValue("id").jsonPrimitive.content,
        started_at = getValue("started_at").jsonPrimitive.long,
        cycle_number = getValue("cycle_number").jsonPrimitive.long,
        source = getValue("source").jsonPrimitive.content,
        status = getValue("status").jsonPrimitive.content,
        level_meta = getValue("level_meta").jsonPrimitive.content,
        goal_meta = getValue("goal_meta").jsonPrimitive.content,
        smart_goal_meta = get("smart_goal_meta")?.jsonPrimitive?.contentOrNull,
        active_flags = getValue("active_flags").jsonPrimitive.content,
        starting_level_per_pattern = getValue("starting_level_per_pattern").jsonPrimitive.content,
        mode = getValue("mode").jsonPrimitive.content,
        checkpoint_days = getValue("checkpoint_days").jsonPrimitive.content,
        deleted_at = get("deleted_at")?.jsonPrimitive?.longOrNull,
        user_id = get("user_id")?.jsonPrimitive?.contentOrNull,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
