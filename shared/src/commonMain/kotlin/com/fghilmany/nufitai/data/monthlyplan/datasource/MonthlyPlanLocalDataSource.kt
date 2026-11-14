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
        planName: String?,
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
                plan_name = planName,
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
                    updated_at = updatedAt,
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
                        updated_at = updatedAt,
                    )
                }
            }
        }
    }

    suspend fun replaceDaysFrom(planId: String, fromDayNumber: Long, days: List<PlanDayInsertRow>, updatedAt: Long) =
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
                        updated_at = updatedAt,
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
                            updated_at = updatedAt,
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

    suspend fun archiveAllActiveExcept(exceptPlanId: String, updatedAt: Long) = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.archiveAllActiveExcept(updatedAt, exceptPlanId)
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
            plan_name = row.plan_name,
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

    /** issue #83 follow-up (ADR-008 §3) -- plan_day sync, mirrors monthly_plan's. */
    suspend fun getPlanDayChangedSince(updatedAfter: Long): List<PlanDayRow> = withContext(Dispatchers.Default) {
        database.planDayQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun getPlanDayById(id: String): PlanDayRow? = withContext(Dispatchers.Default) {
        database.planDayQueries.selectById(id).executeAsOneOrNull()
    }

    suspend fun backfillPlanDayUserId(userId: String) = withContext(Dispatchers.Default) {
        database.planDayQueries.backfillUserId(userId)
    }

    suspend fun upsertPlanDayFromRemote(row: PlanDayRow) = withContext(Dispatchers.Default) {
        database.planDayQueries.upsertFromRemote(
            id = row.id,
            monthly_plan_id = row.monthly_plan_id,
            day_number = row.day_number,
            type = row.type,
            template_letter = row.template_letter,
            warmup_general = row.warmup_general,
            cardio = row.cardio,
            cooldown_heart_rate = row.cooldown_heart_rate,
            user_id = row.user_id,
            updated_at = row.updated_at,
        )
    }

    /** issue #83 follow-up (ADR-008 §3) -- planned_exercise sync, mirrors plan_day's. */
    suspend fun getPlannedExerciseChangedSince(updatedAfter: Long): List<PlannedExerciseRow> = withContext(Dispatchers.Default) {
        database.plannedExerciseQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun getPlannedExerciseById(id: String): PlannedExerciseRow? = withContext(Dispatchers.Default) {
        database.plannedExerciseQueries.selectById(id).executeAsOneOrNull()
    }

    suspend fun backfillPlannedExerciseUserId(userId: String) = withContext(Dispatchers.Default) {
        database.plannedExerciseQueries.backfillUserId(userId)
    }

    suspend fun upsertPlannedExerciseFromRemote(row: PlannedExerciseRow) = withContext(Dispatchers.Default) {
        database.plannedExerciseQueries.upsertFromRemote(
            id = row.id,
            plan_day_id = row.plan_day_id,
            slot = row.slot,
            exercise_id = row.exercise_id,
            sets = row.sets,
            rep_range_or_duration = row.rep_range_or_duration,
            rpe_target_min = row.rpe_target_min,
            rpe_target_max = row.rpe_target_max,
            reason_rule_ids = row.reason_rule_ids,
            rest_seconds = row.rest_seconds,
            user_id = row.user_id,
            updated_at = row.updated_at,
        )
    }

    /** issue #83 follow-up (ADR-008 §3) -- plan_day_session_log sync; columns were already
     * sync-ready (chat-ai.md §7), only the remote-pull side was missing. */
    suspend fun getSessionLogChangedSince(updatedAfter: Long): List<PlanDaySessionLogRow> = withContext(Dispatchers.Default) {
        database.planDaySessionLogQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun getSessionLogById(id: String): PlanDaySessionLogRow? = withContext(Dispatchers.Default) {
        database.planDaySessionLogQueries.selectById(id).executeAsOneOrNull()
    }

    suspend fun backfillSessionLogUserId(userId: String) = withContext(Dispatchers.Default) {
        database.planDaySessionLogQueries.backfillUserId(userId)
    }

    suspend fun upsertSessionLogFromRemote(row: PlanDaySessionLogRow) = withContext(Dispatchers.Default) {
        database.planDaySessionLogQueries.upsertFromRemote(
            id = row.id,
            plan_day_id = row.plan_day_id,
            completed_at = row.completed_at,
            skipped_at = row.skipped_at,
            rpe_reported = row.rpe_reported,
            pain_reported = row.pain_reported,
            user_id = row.user_id,
            updated_at = row.updated_at,
        )
    }
}
