package com.fghilmany.nufitai.data.mealplan.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.core.logging.AppLogger
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.data.mealplan.datasource.MealPlanApiException
import com.fghilmany.nufitai.data.mealplan.datasource.MealPlanLocalDataSource
import com.fghilmany.nufitai.data.mealplan.datasource.MealPlanRemoteDataSource
import com.fghilmany.nufitai.data.mealplan.datasource.PlannedMealInsert
import com.fghilmany.nufitai.data.mealplan.model.MealPlanRequestDto
import com.fghilmany.nufitai.db.Meal_log as MealLogRow
import com.fghilmany.nufitai.db.Meal_plan as MealPlanRow
import com.fghilmany.nufitai.db.Planned_meal as PlannedMealRow
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.mealplan.entity.MacroTarget
import com.fghilmany.nufitai.domain.mealplan.entity.MealLog
import com.fghilmany.nufitai.domain.mealplan.entity.MealLogStatus
import com.fghilmany.nufitai.domain.mealplan.entity.MealPlan
import com.fghilmany.nufitai.domain.mealplan.entity.MealPreference
import com.fghilmany.nufitai.domain.mealplan.entity.MealSlot
import com.fghilmany.nufitai.domain.mealplan.entity.PlannedMeal
import com.fghilmany.nufitai.domain.mealplan.repository.MealPlanRepository
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Instant

// 150s (2026-11-10): matches Supabase Edge Functions' own platform wall-clock cap -- no point
// giving up client-side before the server's own hard limit would. tech-debt-88-pricing-sync.md's
// 2026-11-09 addendum documents Surplus Intelligence's marketplace queueing overhead alone
// (before generation even starts) observed up to ~60s+, on top of ~20-22s generation time.
// NetworkModule.kt's Ktor HttpTimeout (155_000) must always stay ABOVE this.
private const val GENERATE_TIMEOUT_MS = 150_000L

class MealPlanRepositoryImpl(
    private val local: MealPlanLocalDataSource,
    private val remote: MealPlanRemoteDataSource,
    private val authRepository: AuthRepository,
) : MealPlanRepository {
    private val logger = AppLogger.tagged("MealPlanRepository")
    private val json = Json

    override suspend fun getMealPlanForWeek(weekStartDate: Instant): AppResult<MealPlan?> = runCatchingDatabase {
        local.getMealPlanForWeek(weekStartDate.toEpochMilliseconds())?.toEntity()
    }

    override suspend fun getPlannedMeals(mealPlanId: String): AppResult<List<PlannedMeal>> = runCatchingDatabase {
        local.getPlannedMealsForPlan(mealPlanId).map { it.toEntity() }
    }

    override suspend fun getMealLogs(mealPlanId: String): AppResult<List<MealLog>> = runCatchingDatabase {
        local.getMealLogsForPlan(mealPlanId).map { it.toEntity() }
    }

    override suspend fun getPlannedMealById(id: String): AppResult<PlannedMeal?> = runCatchingDatabase {
        local.getPlannedMealById(id)?.toEntity()
    }

    override suspend fun getMealLogForPlannedMeal(plannedMealId: String): AppResult<MealLog?> = runCatchingDatabase {
        local.getMealLogForPlannedMeal(plannedMealId)?.toEntity()
    }

    override suspend fun logMealStatus(plannedMealId: String, status: MealLogStatus): AppResult<Unit> = runCatchingDatabase {
        val existing = local.getMealLogForPlannedMeal(plannedMealId)
        local.upsertMealLog(
            id = existing?.id ?: "$plannedMealId-log",
            plannedMealId = plannedMealId,
            status = status.name,
            loggedAt = currentInstant().toEpochMilliseconds(),
        )
    }

    override suspend fun generateRemote(
        weekStartDate: Instant,
        dayNumber: Int,
        target: MacroTarget,
        goal: GoalCategory,
        weightKgAtGeneration: Double,
        preference: MealPreference,
    ): AppResult<MealPlan> {
        val session = authRepository.getCurrentSession() ?: return AppResult.Error(Failure.Unauthorized())
        logger.i { "generateRemote starting for week=${weekStartDate.toEpochMilliseconds()} day=$dayNumber" }
        return try {
            // AC-8: reuse the existing week's row id (if any) so upsertDayMeals's delete+re-insert
            // targets the right FK-referencing rows instead of orphaning them.
            val existing = local.getMealPlanForWeek(weekStartDate.toEpochMilliseconds())
            val id = existing?.id ?: generateId()

            // Per-day generate (2026-11-10): exactly ONE HTTP call, not a 7-day loop -- shrinks
            // the unit of work so a Doze/background network interruption only costs this one day,
            // not the whole week (see tech-debt-88-pricing-sync.md's 2026-11-10 addendum).
            val response = withTimeoutOrNull(GENERATE_TIMEOUT_MS) {
                remote.generate(MealPlanRequestDto.from(target, goal, preference, dayNumber), session.accessToken)
            } ?: run {
                logger.w { "generateRemote day=$dayNumber timed out after ${GENERATE_TIMEOUT_MS}ms" }
                return AppResult.Error(Failure.Network("Meal plan generation timed out (hari $dayNumber)"))
            }
            val plannedMeals = response.days.flatMap { d -> d.meals.map { it.toEntity(d.dayNumber, id) } }

            val now = currentInstant()

            local.upsertDayMeals(
                id = id,
                weekStartDateMillis = weekStartDate.toEpochMilliseconds(),
                calories = target.calories.toLong(),
                proteinG = target.proteinG.toLong(),
                carbG = target.carbG.toLong(),
                fatG = target.fatG.toLong(),
                weightKgAtGeneration = weightKgAtGeneration,
                goal = goal.name,
                createdAt = now.toEpochMilliseconds(),
                updatedAt = now.toEpochMilliseconds(),
                dayNumber = dayNumber.toLong(),
                meals = plannedMeals.map { it.toInsert() },
            )

            logger.i { "generateRemote succeeded, day=$dayNumber, ${plannedMeals.size} meals" }
            AppResult.Success(
                MealPlan(
                    id = id,
                    weekStartDate = weekStartDate,
                    target = target,
                    weightKgAtGeneration = weightKgAtGeneration,
                    goal = goal,
                    createdAt = now,
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: MealPlanApiException) {
            logger.e(e) { "generateRemote failed -- HTTP ${e.statusCode}" }
            when {
                e.statusCode == 401 -> AppResult.Error(Failure.Unauthorized())
                e.statusCode == 403 && e.quotaExceeded -> AppResult.Error(Failure.QuotaExceeded())
                e.statusCode == 403 -> AppResult.Error(Failure.Validation("Not subscribed"))
                e.statusCode == 422 -> AppResult.Error(Failure.Validation("Meal plan request validation failed"))
                else -> AppResult.Error(Failure.Network("Meal plan backend error (${e.statusCode})"))
            }
        } catch (e: Exception) {
            logger.e(e) { "generateRemote failed -- ${e.message}" }
            AppResult.Error(Failure.Network(e.message ?: "Unknown network error"))
        }
    }

    private fun PlannedMeal.toInsert(): PlannedMealInsert = PlannedMealInsert(
        id = id,
        dayNumber = dayNumber.toLong(),
        slot = slot.name,
        name = name,
        description = description,
        ingredientsJson = json.encodeToString(ingredients),
        calories = macros.calories.toLong(),
        proteinG = macros.proteinG.toLong(),
        carbG = macros.carbG.toLong(),
        fatG = macros.fatG.toLong(),
        prepMinutes = prepMinutes.toLong(),
        tag = tag,
    )

    private fun MealPlanRow.toEntity(): MealPlan = MealPlan(
        id = id,
        weekStartDate = Instant.fromEpochMilliseconds(week_start_date),
        target = MacroTarget(calories = calories.toInt(), proteinG = protein_g.toInt(), carbG = carb_g.toInt(), fatG = fat_g.toInt()),
        weightKgAtGeneration = weight_kg_at_generation,
        goal = GoalCategory.valueOf(goal),
        createdAt = Instant.fromEpochMilliseconds(created_at),
    )

    private fun PlannedMealRow.toEntity(): PlannedMeal = PlannedMeal(
        id = id,
        mealPlanId = meal_plan_id,
        dayNumber = day_number.toInt(),
        slot = MealSlot.valueOf(slot),
        name = name,
        description = description,
        ingredients = json.decodeFromString(ingredients),
        macros = MacroTarget(calories = calories.toInt(), proteinG = protein_g.toInt(), carbG = carb_g.toInt(), fatG = fat_g.toInt()),
        prepMinutes = prep_minutes.toInt(),
        tag = tag,
    )

    private fun MealLogRow.toEntity(): MealLog = MealLog(
        id = id,
        plannedMealId = planned_meal_id,
        status = MealLogStatus.valueOf(status),
        loggedAt = logged_at?.let { Instant.fromEpochMilliseconds(it) },
    )
}
