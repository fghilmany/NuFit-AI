package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlannedExercise
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import com.fghilmany.nufitai.usecase.monthlyplan.rules.BuildMovementPool
import com.fghilmany.nufitai.usecase.monthlyplan.rules.SafetyFilter
import com.fghilmany.nufitai.usecase.monthlyplan.rules.MonthlyScheduler
import com.fghilmany.nufitai.usecase.monthlyplan.rules.Prescription
import com.fghilmany.nufitai.usecase.monthlyplan.rules.GoalPrescription

/**
 * AC-10 / Tahap 7B section E: mid-cycle equipment change reruns only layer 2 (POOL) + downstream
 * for days from [fromDayNumber] forward. `startingLevelPerPattern` and every prior day's history
 * are preserved untouched -- this only rewrites `plan_day`/`planned_exercise` rows >= [fromDayNumber].
 */
class RebuildRemainingDays(
    private val getExercisePool: GetExercisePool,
    private val repository: MonthlyPlanRepository,
) {
    suspend operator fun invoke(
        plan: MonthlyPlan,
        newEquipmentPreference: Set<EquipmentCategory>,
        fromDayNumber: Int,
        overallLevel: Level,
    ): AppResult<Unit> {
        val poolResult = getExercisePool()
        val allExercises = when (poolResult) {
            is AppResult.Success -> poolResult.data
            is AppResult.Error -> return poolResult
        }

        val rawPool = BuildMovementPool(allExercises, newEquipmentPreference, overallLevel)
        val filterResult = SafetyFilter(rawPool, plan.activeFlags, painAreas = emptySet())
        val prescription = GoalPrescription(plan.goalMeta)

        val sessionDaysResult = repository.getPlanDays(plan.id)
        val existingDays = when (sessionDaysResult) {
            is AppResult.Success -> sessionDaysResult.data
            is AppResult.Error -> return sessionDaysResult
        }
        val sessionDayNumbers = existingDays.filter { it.type == DayType.SESSION && it.dayNumber >= fromDayNumber }.map { it.dayNumber }

        val rebuiltDays = (fromDayNumber..30).map { day ->
            if (day !in sessionDayNumbers) {
                PlanDay(generateId(), plan.id, day, DayType.REST, null, null, null, null, null, null)
            } else {
                val sessionIndex = existingDays.indexOfFirst { it.dayNumber == day }.coerceAtLeast(0)
                val templateLetter = MonthlyScheduler.templateFor(sessionIndex)
                val patterns = MonthlyScheduler.TEMPLATE_ROTATION[templateLetter].orEmpty()
                val phase = MonthlyScheduler.phaseFor(day, plan.mode)
                val mainExercises = patterns.mapNotNull { pattern ->
                    buildMainExercise(pattern, filterResult.filteredPool, plan.startingLevelPerPattern[pattern] ?: ExerciseLevel.REGRESSION, prescription, phase.setCount, phase.rpeRange)
                }
                PlanDay(
                    id = generateId(),
                    planId = plan.id,
                    dayNumber = day,
                    type = DayType.SESSION,
                    templateLetter = templateLetter,
                    warmup = MonthlyScheduler.toWarmupBlock(patterns, filterResult.filteredPool, filterResult.mandatoryCorrective),
                    mainExercises = mainExercises,
                    cardio = null,
                    cooldown = MonthlyScheduler.toCooldownBlock(patterns, filterResult.filteredPool, filterResult.mandatoryCorrective),
                    homework = null,
                )
            }
        }

        return repository.replacePlanDaysFrom(plan.id, fromDayNumber, rebuiltDays)
    }

    private fun buildMainExercise(
        pattern: MovementPattern,
        filteredPool: Map<MovementPattern, List<Exercise>>,
        startingLevel: ExerciseLevel,
        prescription: Prescription,
        setCount: Int,
        rpeRange: IntRange,
    ): PlannedExercise? {
        val chosen = filteredPool[pattern].orEmpty().firstOrNull { it.level == startingLevel }
            ?: filteredPool[pattern].orEmpty().firstOrNull()
            ?: return null
        return PlannedExercise(
            exerciseId = chosen.id,
            sets = setCount,
            repRangeOrDuration = "${prescription.repRange.first}-${prescription.repRange.last} rep",
            rpeTargetMin = rpeRange.first,
            rpeTargetMax = rpeRange.last,
            reasonRuleIds = listOf("REBUILD-REMAINING-DAYS"),
        )
    }
}
