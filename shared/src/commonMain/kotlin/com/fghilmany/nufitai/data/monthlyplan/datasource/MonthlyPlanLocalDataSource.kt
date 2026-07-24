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
    val warmupUmumJson: String?,
    val cardioJson: String?,
    val cooldownPenurunanHrJson: String?,
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
        flagsAktifJson: String,
        startingLevelPerPolaJson: String,
        mode: String,
        checkpointDaysJson: String,
        days: List<PlanDayInsertRow>,
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
                flags_aktif = flagsAktifJson,
                starting_level_per_pola = startingLevelPerPolaJson,
                mode = mode,
                checkpoint_days = checkpointDaysJson,
            )
            days.forEach { day ->
                database.planDayQueries.insertPlanDay(
                    id = day.id,
                    monthly_plan_id = id,
                    day_number = day.dayNumber.toLong(),
                    type = day.type,
                    template_letter = day.templateLetter,
                    warmup_umum = day.warmupUmumJson,
                    cardio = day.cardioJson,
                    cooldown_penurunan_hr = day.cooldownPenurunanHrJson,
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
                        warmup_umum = day.warmupUmumJson,
                        cardio = day.cardioJson,
                        cooldown_penurunan_hr = day.cooldownPenurunanHrJson,
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
                        )
                    }
                }
            }
        }

    suspend fun getActivePlan(): MonthlyPlanRow? = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.getActiveMonthlyPlan().executeAsOneOrNull()
    }

    suspend fun archivePlan(planId: String) = withContext(Dispatchers.Default) {
        database.monthlyPlanQueries.archiveMonthlyPlan(planId)
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
    ) = withContext(Dispatchers.Default) {
        database.planDaySessionLogQueries.insertPlanDaySessionLog(
            id = id,
            plan_day_id = planDayId,
            completed_at = completedAt,
            skipped_at = skippedAt,
            rpe_reported = rpeReported,
            pain_reported = painReportedJson,
        )
    }

    suspend fun getSessionLogsForPlan(planId: String): List<PlanDaySessionLogRow> = withContext(Dispatchers.Default) {
        database.planDaySessionLogQueries.getSessionLogsForPlan(planId).executeAsList()
    }
}
