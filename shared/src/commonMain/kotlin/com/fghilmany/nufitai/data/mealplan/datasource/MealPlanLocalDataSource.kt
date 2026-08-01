package com.fghilmany.nufitai.data.mealplan.datasource

import com.fghilmany.nufitai.db.Meal_log as MealLogRow
import com.fghilmany.nufitai.db.Meal_plan as MealPlanRow
import com.fghilmany.nufitai.db.NuFitDatabase
import com.fghilmany.nufitai.db.Planned_meal as PlannedMealRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One meal to insert as part of [MealPlanLocalDataSource.replaceWeek] -- primitive fields only, mirrors this codebase's datasource convention of taking already-serialized values (JSON encoding happens in the repository). */
data class PlannedMealInsert(
    val id: String,
    val dayNumber: Long,
    val slot: String,
    val name: String,
    val description: String,
    val ingredientsJson: String,
    val calories: Long,
    val proteinG: Long,
    val carbG: Long,
    val fatG: Long,
    val prepMinutes: Long,
    val tag: String,
)

class MealPlanLocalDataSource(private val database: NuFitDatabase) {

    suspend fun getMealPlanForWeek(weekStartDateMillis: Long): MealPlanRow? = withContext(Dispatchers.Default) {
        database.mealPlanQueries.getMealPlanForWeek(weekStartDateMillis).executeAsOneOrNull()
    }

    suspend fun getPlannedMealsForPlan(mealPlanId: String): List<PlannedMealRow> = withContext(Dispatchers.Default) {
        database.plannedMealQueries.getPlannedMealsForPlan(mealPlanId).executeAsList()
    }

    suspend fun getPlannedMealById(id: String): PlannedMealRow? = withContext(Dispatchers.Default) {
        database.plannedMealQueries.getPlannedMealById(id).executeAsOneOrNull()
    }

    suspend fun getMealLogsForPlan(mealPlanId: String): List<MealLogRow> = withContext(Dispatchers.Default) {
        database.mealLogQueries.getMealLogsForPlan(mealPlanId).executeAsList()
    }

    suspend fun getMealLogForPlannedMeal(plannedMealId: String): MealLogRow? = withContext(Dispatchers.Default) {
        database.mealLogQueries.getMealLogForPlannedMeal(plannedMealId).executeAsOneOrNull()
    }

    suspend fun upsertMealLog(id: String, plannedMealId: String, status: String, loggedAt: Long?) = withContext(Dispatchers.Default) {
        database.mealLogQueries.upsertMealLog(id, plannedMealId, status, loggedAt)
    }

    /**
     * AC-8 -- atomic replace of one week's meals. [id] must be the existing week's row id when
     * one exists (caller looks it up first via [getMealPlanForWeek]) so the delete+re-insert below
     * targets the right rows and `meal_plan`'s upsert-by-primary-key doesn't collide with its own
     * `week_start_date` UNIQUE index. Deletes `meal_log` before `planned_meal` (its delete query
     * joins through `planned_meal`, mirrors `PlanDay.sq`'s existing delete-then-reinsert convention).
     */
    suspend fun replaceWeek(
        id: String,
        weekStartDateMillis: Long,
        calories: Long,
        proteinG: Long,
        carbG: Long,
        fatG: Long,
        weightKgAtGeneration: Double,
        goal: String,
        createdAt: Long,
        updatedAt: Long,
        meals: List<PlannedMealInsert>,
    ) = withContext(Dispatchers.Default) {
        database.transaction {
            database.mealLogQueries.deleteMealLogsForPlan(id)
            database.plannedMealQueries.deletePlannedMealsForPlan(id)
            database.mealPlanQueries.upsertMealPlan(
                id = id,
                week_start_date = weekStartDateMillis,
                calories = calories,
                protein_g = proteinG,
                carb_g = carbG,
                fat_g = fatG,
                weight_kg_at_generation = weightKgAtGeneration,
                goal = goal,
                created_at = createdAt,
                user_id = null,
                updated_at = updatedAt,
            )
            meals.forEach { meal ->
                database.plannedMealQueries.insertPlannedMeal(
                    id = meal.id,
                    meal_plan_id = id,
                    day_number = meal.dayNumber,
                    slot = meal.slot,
                    name = meal.name,
                    description = meal.description,
                    ingredients = meal.ingredientsJson,
                    calories = meal.calories,
                    protein_g = meal.proteinG,
                    carb_g = meal.carbG,
                    fat_g = meal.fatG,
                    prep_minutes = meal.prepMinutes,
                    tag = meal.tag,
                )
                database.mealLogQueries.upsertMealLog(id = "${meal.id}-log", planned_meal_id = meal.id, status = "PENDING", logged_at = null)
            }
        }
    }

    /** techspec meal-plan.md §6 -- sync engine cursor query, mirrors MonthlyPlanLocalDataSource's shape. Header row only this pass. */
    suspend fun getChangedSince(updatedAfter: Long): List<MealPlanRow> = withContext(Dispatchers.Default) {
        database.mealPlanQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun getById(id: String): MealPlanRow? = withContext(Dispatchers.Default) {
        database.mealPlanQueries.selectById(id).executeAsOneOrNull()
    }

    suspend fun backfillUserId(userId: String) = withContext(Dispatchers.Default) {
        database.mealPlanQueries.backfillUserId(userId)
    }

    /** Remote-sync upsert; unlike [replaceWeek] (local generate, always `user_id = null`), this preserves the remote row's `user_id`. */
    suspend fun upsertMealPlanFromRemote(row: MealPlanRow) = withContext(Dispatchers.Default) {
        database.mealPlanQueries.upsertMealPlan(
            id = row.id,
            week_start_date = row.week_start_date,
            calories = row.calories,
            protein_g = row.protein_g,
            carb_g = row.carb_g,
            fat_g = row.fat_g,
            weight_kg_at_generation = row.weight_kg_at_generation,
            goal = row.goal,
            created_at = row.created_at,
            user_id = row.user_id,
            updated_at = row.updated_at,
        )
    }
}
