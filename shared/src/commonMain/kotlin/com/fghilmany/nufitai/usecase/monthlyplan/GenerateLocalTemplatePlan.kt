package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlannedExercise
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import com.fghilmany.nufitai.usecase.monthlyplan.rules.BuildMovementPool
import com.fghilmany.nufitai.usecase.monthlyplan.rules.MonthlyScheduler
import com.fghilmany.nufitai.usecase.monthlyplan.rules.Prescription
import com.fghilmany.nufitai.usecase.monthlyplan.rules.GoalPrescription

private val UPPER_PATTERNS = listOf(MovementPattern.PUSH_HORIZONTAL, MovementPattern.PUSH_VERTICAL, MovementPattern.PULL_HORIZONTAL, MovementPattern.CORE)
private val LOWER_PATTERNS = listOf(MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.LUNGE, MovementPattern.CORE)
private const val LOW_FREQUENCY_REST_GAP_THRESHOLD = 3

/**
 * Local tier: template matrix (00-overview.md Keputusan #3), reuses `QuickAssessmentResult`.
 * Skips GATE (already applied at Quick Assessment time)/SAFETY (no postural/movement flags
 * collected)/CALIBRATION (no capacity test)/PROGRESSION-GATING (fixed phase table only) --
 * per issue #76 §5. Correction vs the techspec's stated assumption: `QuickAssessmentAnswer`
 * DOES collect `equipment` (wizard step 3, `EquipmentCopy.kt`) -- used here rather than a
 * hardcoded Bodyweight-only default.
 */
class GenerateLocalTemplatePlan(
    private val getExercisePool: GetExercisePool,
    private val repository: MonthlyPlanRepository,
) {
    suspend operator fun invoke(quickAssessment: QuickAssessmentResult): AppResult<MonthlyPlan> {
        val poolResult = getExercisePool()
        val allExercises = when (poolResult) {
            is AppResult.Success -> poolResult.data
            is AppResult.Error -> return poolResult
        }

        val pool = BuildMovementPool(allExercises, quickAssessment.input.equipment, quickAssessment.level)
        val startingLevel = if (quickAssessment.level == Level.BEGINNER) ExerciseLevel.REGRESSION else ExerciseLevel.STANDARD
        val prescription = GoalPrescription(quickAssessment.input.goal)

        val sessionsPerWeek = quickAssessment.input.frequency.sessionsPerWeek
        val selectedWeekdays = evenlySpreadWeekdays(sessionsPerWeek)
        // The mandatory rest-gap is a Beginner-only safety nudge and is mathematically
        // infeasible to honor once >=4 sessions/week are requested within a 7-day week
        // (guaranteed adjacency) -- restrict it to where it's both meaningful and achievable.
        val minRestBetweenSessions = sessionsPerWeek <= LOW_FREQUENCY_REST_GAP_THRESHOLD && quickAssessment.level == Level.BEGINNER
        val sessionDays = MonthlyScheduler.buildCalendarFramework(sessionsPerWeek, selectedWeekdays, minRestBetweenSessions)

        val planId = generateId()
        val startingLevelPerPattern = MovementPattern.entries
            .filter { it != MovementPattern.CORRECTIVE && it != MovementPattern.STRETCH && it != MovementPattern.CARDIO }
            .associateWith { startingLevel }

        val plan = MonthlyPlan(
            id = planId,
            startedAt = currentInstant(),
            cycleNumber = 1,
            source = PlanSource.LOCAL_TEMPLATE,
            status = PlanStatus.ACTIVE,
            levelMeta = quickAssessment.level.name,
            goalMeta = quickAssessment.input.goal,
            smartGoalMeta = null,
            activeFlags = emptySet(),
            startingLevelPerPattern = startingLevelPerPattern,
            mode = ProgressionMode.NORMAL,
            checkpointDays = emptyList(), // Local tier: no progression gating, fixed phase table only
        )

        val patternsForSplit = if (quickAssessment.resolvedSplit == ResolvedSplit.FULL_BODY) {
            List(sessionDays.size) { MonthlyScheduler.templateFor(it) to (MonthlyScheduler.TEMPLATE_ROTATION[MonthlyScheduler.templateFor(it)] ?: emptyList()) }
        } else {
            List(sessionDays.size) { index ->
                if (index % 2 == 0) "U" to UPPER_PATTERNS else "L" to LOWER_PATTERNS
            }
        }

        val days = (1..30).map { day ->
            val sessionIndex = sessionDays.indexOf(day)
            if (sessionIndex == -1) {
                PlanDay(generateId(), planId, day, DayType.REST, null, null, null, null, null, null)
            } else {
                val (templateLetter, patterns) = patternsForSplit[sessionIndex]
                val phase = MonthlyScheduler.phaseFor(day, ProgressionMode.NORMAL)
                val mainExercises = patterns.mapNotNull { pattern -> buildMainExercise(pattern, pool, startingLevel, prescription, phase.setCount, phase.rpeRange) }
                PlanDay(
                    id = generateId(),
                    planId = planId,
                    dayNumber = day,
                    type = DayType.SESSION,
                    templateLetter = templateLetter,
                    warmup = MonthlyScheduler.toWarmupBlock(patterns, pool, mandatoryCorrective = emptyList()),
                    mainExercises = mainExercises,
                    cardio = null,
                    cooldown = MonthlyScheduler.toCooldownBlock(patterns, pool, mandatoryCorrective = emptyList()),
                    homework = null,
                )
            }
        }

        return when (val saved = repository.savePlan(plan, days)) {
            is AppResult.Success -> AppResult.Success(plan)
            is AppResult.Error -> saved
        }
    }

    private fun buildMainExercise(
        pattern: MovementPattern,
        pool: Map<MovementPattern, List<Exercise>>,
        startingLevel: ExerciseLevel,
        prescription: Prescription,
        setCount: Int,
        rpeRange: IntRange,
    ): PlannedExercise? {
        val candidates = pool[pattern].orEmpty()
        val chosen = candidates.firstOrNull { it.level == startingLevel } ?: candidates.firstOrNull() ?: return null
        return PlannedExercise(
            exerciseId = chosen.id,
            sets = setCount,
            repRangeOrDuration = "${prescription.repRange.first}-${prescription.repRange.last} rep",
            rpeTargetMin = rpeRange.first,
            rpeTargetMax = rpeRange.last,
            reasonRuleIds = listOf("LOCAL-TEMPLATE"),
        )
    }
}

private fun evenlySpreadWeekdays(sessionsPerWeek: Int): Set<Int> = when (sessionsPerWeek) {
    1 -> setOf(3) // Wed
    2 -> setOf(2, 5) // Tue/Fri
    3 -> setOf(1, 3, 5) // Mon/Wed/Fri
    4 -> setOf(1, 3, 5, 7) // Mon/Wed/Fri/Sun -- gap-separated
    5 -> setOf(1, 2, 3, 4, 5) // Mon-Fri
    6 -> setOf(1, 2, 3, 4, 5, 6) // Mon-Sat
    7 -> setOf(1, 2, 3, 4, 5, 6, 7) // every day
    else -> setOf(1, 3, 5)
}
