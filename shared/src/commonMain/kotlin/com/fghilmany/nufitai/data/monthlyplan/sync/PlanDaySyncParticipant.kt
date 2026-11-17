package com.fghilmany.nufitai.data.monthlyplan.sync

import com.fghilmany.nufitai.data.monthlyplan.datasource.MonthlyPlanLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Plan_day as PlanDayRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.time.Instant

/**
 * ADR-008 §3 follow-up -- plan_day was the documented "header-only sync" gap: a plan restored
 * after uninstall/new-device login came back with no sessions, since only the monthly_plan row
 * itself synced. Mirrors MonthlyPlanSyncParticipant's shape exactly. Must be registered AFTER
 * MonthlyPlanSyncParticipant in SyncModule's participant list -- push/pull run sequentially per
 * participant, and plan_day has a local FK to monthly_plan(id).
 */
class PlanDaySyncParticipant(
    private val local: MonthlyPlanLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "plan_day"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getPlanDayChangedSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        local.upsertPlanDaysFromRemote(rows.map { it.toRow() })
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillPlanDayUserId(userId)
    }

    private fun PlanDayRow.toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("monthly_plan_id", JsonPrimitive(monthly_plan_id))
        put("day_number", JsonPrimitive(day_number))
        put("type", JsonPrimitive(type))
        put("template_letter", JsonPrimitive(template_letter))
        put("warmup_general", JsonPrimitive(warmup_general))
        put("cardio", JsonPrimitive(cardio))
        put("cooldown_heart_rate", JsonPrimitive(cooldown_heart_rate))
        put("user_id", JsonPrimitive(user_id))
        put("updated_at", JsonPrimitive(updated_at))
    }

    private fun JsonObject.toRow(): PlanDayRow = PlanDayRow(
        id = getValue("id").jsonPrimitive.content,
        monthly_plan_id = getValue("monthly_plan_id").jsonPrimitive.content,
        day_number = getValue("day_number").jsonPrimitive.long,
        type = getValue("type").jsonPrimitive.content,
        template_letter = get("template_letter")?.jsonPrimitive?.contentOrNull,
        warmup_general = get("warmup_general")?.jsonPrimitive?.contentOrNull,
        cardio = get("cardio")?.jsonPrimitive?.contentOrNull,
        cooldown_heart_rate = get("cooldown_heart_rate")?.jsonPrimitive?.contentOrNull,
        user_id = get("user_id")?.jsonPrimitive?.contentOrNull,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
