package com.fghilmany.nufitai.data.mealplan.sync

import com.fghilmany.nufitai.data.mealplan.datasource.MealPlanLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Meal_plan as MealPlanRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.time.Instant

/**
 * techspec meal-plan.md §6/§7 -- real from day one (not a stub-then-fix-later pattern, per the
 * techspec's own DoD item calling out `FullAssessmentSyncParticipant`/`WorkoutHistorySyncParticipant`'s
 * retrofit history in chat-ai.md). Mirrors `MonthlyPlanSyncParticipant`'s worked-example pattern.
 * Scope note: syncs the `meal_plan` header row only -- nested `planned_meal`/`meal_log` sync is a
 * documented follow-up, same precedent as `WorkoutHistorySyncParticipant`.
 */
class MealPlanSyncParticipant(
    private val local: MealPlanLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "meal_plan"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getChangedSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        rows.forEach { row ->
            val id = row.getValue("id").jsonPrimitive.content
            val remoteUpdatedAt = row.getValue("updated_at").jsonPrimitive.long
            val existing = local.getById(id)
            if (existing == null || existing.updated_at < remoteUpdatedAt) {
                local.upsertMealPlanFromRemote(row.toRow())
            }
        }
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillUserId(userId)
    }

    private fun MealPlanRow.toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("week_start_date", JsonPrimitive(week_start_date))
        put("calories", JsonPrimitive(calories))
        put("protein_g", JsonPrimitive(protein_g))
        put("carb_g", JsonPrimitive(carb_g))
        put("fat_g", JsonPrimitive(fat_g))
        put("weight_kg_at_generation", JsonPrimitive(weight_kg_at_generation))
        put("goal", JsonPrimitive(goal))
        put("created_at", JsonPrimitive(created_at))
        put("user_id", JsonPrimitive(user_id))
        put("updated_at", JsonPrimitive(updated_at))
    }

    private fun JsonObject.toRow(): MealPlanRow = MealPlanRow(
        id = getValue("id").jsonPrimitive.content,
        week_start_date = getValue("week_start_date").jsonPrimitive.long,
        calories = getValue("calories").jsonPrimitive.long,
        protein_g = getValue("protein_g").jsonPrimitive.long,
        carb_g = getValue("carb_g").jsonPrimitive.long,
        fat_g = getValue("fat_g").jsonPrimitive.long,
        weight_kg_at_generation = getValue("weight_kg_at_generation").jsonPrimitive.double,
        goal = getValue("goal").jsonPrimitive.content,
        created_at = getValue("created_at").jsonPrimitive.long,
        user_id = get("user_id")?.jsonPrimitive?.contentOrNull,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
