package com.fghilmany.nufitai.data.monthlyplan.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.data.monthlyplan.datasource.MonthlyPlanLocalDataSource
import com.fghilmany.nufitai.data.monthlyplan.datasource.PlanDayInsertRow
import com.fghilmany.nufitai.data.monthlyplan.datasource.PlannedExerciseInsertRow
import com.fghilmany.nufitai.data.monthlyplan.model.CardioBlockJson
import com.fghilmany.nufitai.db.Monthly_plan as MonthlyPlanRow
import com.fghilmany.nufitai.db.Plan_day as PlanDayRow
import com.fghilmany.nufitai.db.Plan_day_session_log as PlanDaySessionLogRow
import com.fghilmany.nufitai.db.Planned_exercise as PlannedExerciseRow
import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.monthlyplan.entity.CooldownBlock
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlannedExercise
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.domain.monthlyplan.entity.WarmupBlock
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class MonthlyPlanRepositoryImpl(
    private val local: MonthlyPlanLocalDataSource,
) : MonthlyPlanRepository {

    private val json = Json

    override suspend fun savePlan(plan: MonthlyPlan, days: List<PlanDay>): AppResult<Unit> = runCatchingDatabase {
        local.insertPlan(
            id = plan.id,
            startedAt = plan.startedAt.toEpochMilliseconds(),
            cycleNumber = plan.cycleNumber.toLong(),
            source = plan.source.name,
            status = plan.status.name,
            levelMeta = plan.levelMeta,
            goalMeta = plan.goalMeta.name,
            smartGoalMeta = plan.smartGoalMeta,
            activeFlagsJson = json.encodeToString(plan.activeFlags),
            startingLevelPerPatternJson = json.encodeToString(plan.startingLevelPerPattern),
            mode = plan.mode.name,
            checkpointDaysJson = json.encodeToString(plan.checkpointDays),
            days = days.map { it.toInsertRow() },
        )
    }

    override suspend fun getActivePlan(): AppResult<MonthlyPlan?> = runCatchingDatabase {
        local.getActivePlan()?.toEntity()
    }

    override suspend fun getAllPlans(): AppResult<List<MonthlyPlan>> = runCatchingDatabase {
        local.getAllPlans().map { it.toEntity() }
    }

    override suspend fun getPlanDays(planId: String): AppResult<List<PlanDay>> = runCatchingDatabase {
        val dayRows = local.getPlanDays(planId)
        val exerciseRows = local.getPlannedExercisesForDays(dayRows.map { it.id })
        val exercisesByDay = exerciseRows.groupBy { it.plan_day_id }
        dayRows.map { it.toEntity(exercisesByDay[it.id].orEmpty()) }
    }

    override suspend fun archivePlan(planId: String): AppResult<Unit> = runCatchingDatabase {
        local.archivePlan(planId)
    }

    override suspend fun replacePlanDaysFrom(planId: String, fromDayNumber: Int, days: List<PlanDay>): AppResult<Unit> =
        runCatchingDatabase {
            local.replaceDaysFrom(planId, fromDayNumber.toLong(), days.map { it.toInsertRow() })
        }

    override suspend fun saveSessionLog(log: PlanDaySessionLog): AppResult<Unit> = runCatchingDatabase {
        local.insertSessionLog(
            id = log.id,
            planDayId = log.planDayId,
            completedAt = log.completedAt?.toEpochMilliseconds(),
            skippedAt = log.skippedAt?.toEpochMilliseconds(),
            rpeReported = log.rpeReported?.toLong(),
            painReportedJson = log.painReported?.let { json.encodeToString(it) },
        )
    }

    override suspend fun getSessionLogsForPlan(planId: String): AppResult<List<PlanDaySessionLog>> = runCatchingDatabase {
        local.getSessionLogsForPlan(planId).map { it.toEntity() }
    }

    private fun PlanDay.toInsertRow(): PlanDayInsertRow {
        val exercises = buildList {
            warmup?.specific?.forEach { add(it.toInsertRow("warmup_specific")) }
            warmup?.corrective?.forEach { add(it.toInsertRow("warmup_corrective")) }
            mainExercises?.forEach { add(it.toInsertRow("main")) }
            cooldown?.stretch?.forEach { add(it.toInsertRow("cooldown_stretch")) }
            homework?.forEach { add(it.toInsertRow("homework")) }
        }
        return PlanDayInsertRow(
            id = id,
            dayNumber = dayNumber,
            type = type.name,
            templateLetter = templateLetter,
            warmupGeneralJson = warmup?.general?.let { json.encodeToString(CardioBlockJson.from(it)) },
            cardioJson = cardio?.let { json.encodeToString(CardioBlockJson.from(it)) },
            cooldownHeartRateJson = cooldown?.heartRateCooldown?.let { json.encodeToString(CardioBlockJson.from(it)) },
            plannedExercises = exercises,
        )
    }

    private fun PlannedExercise.toInsertRow(slot: String) = PlannedExerciseInsertRow(
        id = generateId(),
        slot = slot,
        exerciseId = exerciseId,
        sets = sets,
        repRangeOrDuration = repRangeOrDuration,
        rpeTargetMin = rpeTargetMin,
        rpeTargetMax = rpeTargetMax,
        reasonRuleIdsJson = json.encodeToString(reasonRuleIds),
        restSeconds = restSeconds,
    )

    private fun MonthlyPlanRow.toEntity(): MonthlyPlan = MonthlyPlan(
        id = id,
        startedAt = Instant.fromEpochMilliseconds(started_at),
        cycleNumber = cycle_number.toInt(),
        source = PlanSource.valueOf(source),
        status = PlanStatus.valueOf(status),
        levelMeta = level_meta,
        goalMeta = GoalCategory.valueOf(goal_meta),
        smartGoalMeta = smart_goal_meta,
        activeFlags = json.decodeFromString<Set<ExerciseFlag>>(active_flags),
        startingLevelPerPattern = json.decodeFromString<Map<MovementPattern, ExerciseLevel>>(starting_level_per_pattern),
        mode = ProgressionMode.valueOf(mode),
        checkpointDays = json.decodeFromString<List<Int>>(checkpoint_days),
    )

    private fun PlanDayRow.toEntity(exerciseRows: List<PlannedExerciseRow>): PlanDay {
        val bySlot = exerciseRows.groupBy { it.slot }
        val specificWarmup = bySlot["warmup_specific"].orEmpty().map { it.toEntity() }
        val warmupCorrective = bySlot["warmup_corrective"].orEmpty().map { it.toEntity() }
        val main = bySlot["main"].orEmpty().map { it.toEntity() }
        val cooldownStretch = bySlot["cooldown_stretch"].orEmpty().map { it.toEntity() }
        val homeworkExercises = bySlot["homework"].orEmpty().map { it.toEntity() }

        val generalWarmupBlock = warmup_general?.let { json.decodeFromString<CardioBlockJson>(it).toEntity() }
        val cooldownHrBlock = cooldown_heart_rate?.let { json.decodeFromString<CardioBlockJson>(it).toEntity() }

        return PlanDay(
            id = id,
            planId = monthly_plan_id,
            dayNumber = day_number.toInt(),
            type = DayType.valueOf(type),
            templateLetter = template_letter,
            warmup = generalWarmupBlock?.let { WarmupBlock(it, specificWarmup, warmupCorrective) },
            mainExercises = main.ifEmpty { null },
            cardio = cardio?.let { json.decodeFromString<CardioBlockJson>(it).toEntity() },
            cooldown = cooldownHrBlock?.let { CooldownBlock(it, cooldownStretch) },
            homework = homeworkExercises.ifEmpty { null },
        )
    }

    private fun PlannedExerciseRow.toEntity(): PlannedExercise = PlannedExercise(
        exerciseId = exercise_id,
        sets = sets.toInt(),
        repRangeOrDuration = rep_range_or_duration,
        rpeTargetMin = rpe_target_min.toInt(),
        rpeTargetMax = rpe_target_max.toInt(),
        reasonRuleIds = json.decodeFromString<List<String>>(reason_rule_ids),
        restSeconds = rest_seconds?.toInt(),
    )

    private fun PlanDaySessionLogRow.toEntity(): PlanDaySessionLog = PlanDaySessionLog(
        id = id,
        planDayId = plan_day_id,
        completedAt = completed_at?.let { Instant.fromEpochMilliseconds(it) },
        skippedAt = skipped_at?.let { Instant.fromEpochMilliseconds(it) },
        rpeReported = rpe_reported?.toInt(),
        painReported = pain_reported?.let { json.decodeFromString<Set<BodyArea>>(it) },
    )
}
