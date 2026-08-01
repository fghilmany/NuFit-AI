package com.fghilmany.nufitai.data.monthlyplan.datasource

import com.fghilmany.nufitai.db.Monthly_plan as MonthlyPlanRow
import com.fghilmany.nufitai.db.NuFitDatabase
import com.fghilmany.nufitai.db.Plan_day as PlanDayRow
import com.fghilmany.nufitai.db.Plan_day_session_log as PlanDaySessionLogRow
import com.fghilmany.nufitai.db.Planned_exercise as PlannedExerciseRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PlanDayInsertRow(
    val id: String,
    val dayNumber: Int,
    val type: String,
    val templateLetter: String?,
    val warmupGeneralJson: String?,
    val cardioJson: String?,
    val cooldownHeartRateJson: String?,
    val plannedExercises: List<PlannedExerciseInsertRow>,
)

data class PlannedExerciseInsertRow(
    val id: String,
    val slot: String,
    val exerciseId: String,
    val sets: Int,
    val repRangeOrDuration: String,
    val rpeTargetMin: Int,
    val rpeTargetMax: Int,
    val reasonRuleIdsJson: String,
    val restSeconds: Int?,
)

class MonthlyPlanLocalDataSource(private val database: NuFitDatabase) {

    suspend fun insertPlan(
        id: String,
        startedAt: Long,
        cycleNumber: Long,
        source: String,
        status: String,
        levelMeta: String,
        goalMeta: String,
        smartGoalMeta: String?,
        activeFlagsJson: String,
        startingLevelPerPatternJson: String,
        mode: String,
        checkpointDaysJson: String,
        days: List<PlanDayInsertRow>,
        updatedAt: Long,
    ) = withContext(Dispatchers.Default) {
        database.transaction {
            database.monthlyPlanQueries.insertMonthlyPlan(
                id = id,
                started_at = startedAt,
                cycle_number = cycleNumber,
                source = source,
                status = status,
                level_meta = levelMeta,
                goal_meta = goalMeta,
                smart_goal_meta = smartGoalMeta,
                active_flags = activeFlagsJson,
                starting_level_per_pattern = startingLevelPerPatternJson,
                mode = mode,
                checkpoint_days = checkpointDaysJson,
                updated_at = updatedAt,
            )
            days.forEach { day ->
                database.planDayQueries.insertPlanDay(
                    id = day.id,
                    monthly_plan_id = id,
                    day_number = day.dayNumber.toLong(),
                    type = day.type,
                    template_letter = day.templateLetter,
                    warmup_general = day.warmupGeneralJson,
                    cardio = day.cardioJson,
                    cooldown_heart_rate = day.cooldownHeartRateJson,
                )
                day.plannedExercises.forEach { exercise ->
                    database.plannedExerciseQueries.insertPlannedExercise(
                        id = exercise.id,
                        plan_day_id = day.id,
                        slot = exercise.slot,
                        exercise_id = exercise.exerciseId,
                        sets = exercise.sets.toLong(),
                        rep_range_or_duration = exercise.repRangeOrDuration,
                        rpe_target_min = exercise.rpeTargetMin.toLong(),
                        rpe_target_max = exercise.rpeTargetMax.toLong(),
                        reason_rule_ids = exercise.reasonRuleIdsJson,
                        rest_seconds = exercise.restSeconds?.toLong(),
                    )
                }
            }
        }
    }

    suspend fun replaceDaysFrom(planId: String, fromDayNumber: Long, days: List<PlanDayInsertRow>) =
        withContext(Dispatchers.Default) {
            database.transaction {
                val staleDayIds = database.planDayQueries.getPlanDayIdsFromDay(planId, fromDayNumber).executeAsList()
                if (staleDayIds.isNotEmpty()) {
                    database.plannedExerciseQueries.deletePlannedExercisesForDays(staleDayIds)
                }
                database.planDayQueries.deletePlanDaysFromDay(planId, fromDayNumber)
                days.forEach { day ->
                    database.planDayQueries.insertPlanDay(
                        id = day.id,
                        monthly_plan_id = planId,
                        day_number = day.dayNumber.toLong(),
                        type = day.type,
                        template_letter = day.templateLetter,
                        warmup_general = day.warmupGeneralJson,
                        cardio = day.cardioJson,
                        cooldown_heart_rate = day.cooldownHeartRateJson,
                    )
                    day.plannedExercises.forEach { exercise ->
                        database.plannedExerciseQueries.insertPlannedExercise(
                            id = exercise.id,
                            plan_day_id = day.id,
                            slot = exercise.slot,
                            exercise_id = exercise.exerciseId,
                            sets = exercise.sets.toLong(),
                            rep_range_or_duration = exercise.repRangeOrDuration,
                            rpe_target_min = exercise.rpeTargetMin.toLong(),
                            rpe_target_max = exercise.rpeTargetMax.toLong(),
                            reason_rule_ids = exercise.reasonRuleIdsJson,
                            rest_seconds = exercise.restSeconds?.toLong(),
                        )
                    }
                }
            }
        }

    suspend fun getActivePlan(): MonthlyPlanRow? = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.getActiveMonthlyPlan().executeAsOneOrNull()
    }

    suspend fun getAllPlans(): List<MonthlyPlanRow> = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.getAllMonthlyPlans().executeAsList()
    }

    suspend fun archivePlan(planId: String, updatedAt: Long) = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.archiveMonthlyPlan(updatedAt, planId)
    }

    suspend fun activatePlan(planId: String, updatedAt: Long) = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.activateMonthlyPlan(updatedAt, planId)
    }

    suspend fun softDeletePlan(planId: String, deletedAt: Long) = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.softDeleteMonthlyPlan(deletedAt, deletedAt, planId)
    }

    /** issue #83 -- sync engine (techspec §7). */
    suspend fun getChangedSince(updatedAfter: Long): List<MonthlyPlanRow> = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun getById(id: String): MonthlyPlanRow? = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.selectById(id).executeAsOneOrNull()
    }

    suspend fun backfillUserId(userId: String) = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.backfillUserId(userId)
    }

    suspend fun upsertFromRemote(row: MonthlyPlanRow) = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.upsertFromRemote(
            id = row.id,
            started_at = row.started_at,
            cycle_number = row.cycle_number,
            source = row.source,
            status = row.status,
            level_meta = row.level_meta,
            goal_meta = row.goal_meta,
            smart_goal_meta = row.smart_goal_meta,
            active_flags = row.active_flags,
            starting_level_per_pattern = row.starting_level_per_pattern,
            mode = row.mode,
            checkpoint_days = row.checkpoint_days,
            deleted_at = row.deleted_at,
            user_id = row.user_id,
            updated_at = row.updated_at,
        )
    }

    suspend fun getPlanDays(planId: String): List<PlanDayRow> = withContext(Dispatchers.Default) {
        database.planDayQueries.getPlanDaysForPlan(planId).executeAsList()
    }

    suspend fun getPlannedExercisesForDays(dayIds: List<String>): List<PlannedExerciseRow> = withContext(Dispatchers.Default) {
        if (dayIds.isEmpty()) return@withContext emptyList()
        database.plannedExerciseQueries.getPlannedExercisesForDays(dayIds).executeAsList()
    }

    suspend fun insertSessionLog(
        id: String,
        planDayId: String,
        completedAt: Long?,
        skippedAt: Long?,
        rpeReported: Long?,
        painReportedJson: String?,
        updatedAt: Long,
    ) = withContext(Dispatchers.Default) {
        database.planDaySessionLogQueries.insertPlanDaySessionLog(
            id = id,
            plan_day_id = planDayId,
            completed_at = completedAt,
            skipped_at = skippedAt,
            rpe_reported = rpeReported,
            pain_reported = painReportedJson,
            user_id = null,
            updated_at = updatedAt,
        )
    }

    suspend fun getSessionLogsForPlan(planId: String): List<PlanDaySessionLogRow> = withContext(Dispatchers.Default) {
        database.planDaySessionLogQueries.getSessionLogsForPlan(planId).executeAsList()
    }
}
