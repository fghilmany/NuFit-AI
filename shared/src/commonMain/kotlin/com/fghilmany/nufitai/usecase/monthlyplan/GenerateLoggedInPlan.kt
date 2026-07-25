package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlannedExercise
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import com.fghilmany.nufitai.usecase.monthlyplan.rules.BuildMovementPool
import com.fghilmany.nufitai.usecase.monthlyplan.rules.SafetyFilter
import com.fghilmany.nufitai.usecase.monthlyplan.rules.ProgressionGating
import com.fghilmany.nufitai.usecase.monthlyplan.rules.CalibrateStartingLevel
import com.fghilmany.nufitai.usecase.monthlyplan.rules.MonthlyScheduler
import com.fghilmany.nufitai.usecase.monthlyplan.rules.Prescription
import com.fghilmany.nufitai.usecase.monthlyplan.rules.GoalPrescription

private val STARTING_LEVEL_PATTERNS = listOf(
    MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.PUSH_HORIZONTAL, MovementPattern.PUSH_VERTICAL,
    MovementPattern.PULL_HORIZONTAL, MovementPattern.PULL_VERTICAL, MovementPattern.CORE, MovementPattern.CARRY,
    MovementPattern.LUNGE,
)
private val DIRECT_TEST_PATTERNS = setOf(
    MovementPattern.SQUAT, MovementPattern.LUNGE, MovementPattern.PUSH_HORIZONTAL, MovementPattern.PUSH_VERTICAL, MovementPattern.CORE,
)

/**
 * Logged-In tier: full tahap-7b 8-layer pipeline (issue #29/#18). Layer 1 (GATE) already
 * applied by `usecase/fullassessment/SubmitFullAssessmentParQ`. Uses full-body A/B/C rotation
 * for all users (issue #19's Intermediate+ Upper/Lower/PPL frequency*fokus split matrix is not
 * implemented -- documented simplification, out of this pass's scope). Capacity-test score
 * categorization always resolves to `null` (see `CalibrateStartingLevel`'s doc: norm tables
 * from `tahap-5-tes-kapasitas-fisik.md` were not available) -- CAL-05's safe default applies.
 */
class GenerateLoggedInPlan(
    private val getExercisePool: GetExercisePool,
    private val repository: MonthlyPlanRepository,
) {
    suspend operator fun invoke(fullAssessment: FullAssessmentResult, overallLevel: Level): AppResult<MonthlyPlan> {
        val poolResult = getExercisePool()
        val allExercises = when (poolResult) {
            is AppResult.Success -> poolResult.data
            is AppResult.Error -> return poolResult
        }

        val rawPool = BuildMovementPool(allExercises, fullAssessment.equipmentPreference, overallLevel)
        val activeFlags = fullAssessment.parQCategoryB + fullAssessment.flagsPostural + fullAssessment.movementFlags
        val filterResult = SafetyFilter(rawPool, activeFlags, fullAssessment.injuryHistory)
        val prescription = GoalPrescription(fullAssessment.goal)

        // PROG-06: conservative mode when GATE-02 fired (any parQCategoryB flag) OR activeFlags >= 3.
        val mode = if (fullAssessment.parQCategoryB.isNotEmpty() || activeFlags.size >= 3) ProgressionMode.CONSERVATIVE else ProgressionMode.NORMAL
        val checkpointDays = ProgressionGating.checkpointDays(mode)

        val startingLevelPerPattern = STARTING_LEVEL_PATTERNS.associateWith { pattern ->
            val hasExclusion = (filterResult.filteredPool[pattern]?.size ?: 0) < (rawPool[pattern]?.size ?: 0) // CAL-01 proxy
            CalibrateStartingLevel(
                hasExclusionFlagForPattern = hasExclusion,
                hasDirectTest = pattern in DIRECT_TEST_PATTERNS,
                scoreCategory = null, // norm tables unavailable -- see class doc
                level = overallLevel,
            )
        }

        val planId = generateId()
        // Same rest-gap rationale as GenerateLocalTemplatePlan: a Beginner-only safety nudge,
        // achievable only at low frequency -- see LOW_FREQUENCY_REST_GAP_THRESHOLD there.
        val minRestBetweenSessions = overallLevel == Level.BEGINNER && fullAssessment.sessionsPerWeek <= 3
        val sessionDays = MonthlyScheduler.buildCalendarFramework(
            sessionsPerWeek = fullAssessment.sessionsPerWeek,
            selectedWeekdays = fullAssessment.selectedWeekdays,
            minRestBetweenSessions = minRestBetweenSessions,
        )

        val plan = MonthlyPlan(
            id = planId,
            startedAt = currentInstant(),
            cycleNumber = 1,
            source = PlanSource.LOGGED_IN_RULE_ENGINE,
            status = PlanStatus.ACTIVE,
            levelMeta = overallLevel.name,
            goalMeta = fullAssessment.goal,
            smartGoalMeta = null,
            activeFlags = activeFlags,
            startingLevelPerPattern = startingLevelPerPattern,
            mode = mode,
            checkpointDays = checkpointDays,
        )

        val days = (1..30).map { day ->
            val sessionIndex = sessionDays.indexOf(day)
            if (sessionIndex == -1) {
                PlanDay(generateId(), planId, day, DayType.REST, null, null, null, null, null, null)
            } else {
                buildSessionDay(day, sessionIndex, planId, filterResult.filteredPool, filterResult.mandatoryCorrective, startingLevelPerPattern, prescription, mode, rawPool = rawPool, allExercises = allExercises)
            }
        }

        return when (val saved = repository.savePlan(plan, days)) {
            is AppResult.Success -> AppResult.Success(plan)
            is AppResult.Error -> saved
        }
    }

    private fun buildSessionDay(
        day: Int,
        sessionIndex: Int,
        planId: String,
        filteredPool: Map<MovementPattern, List<Exercise>>,
        mandatoryCorrective: List<Exercise>,
        startingLevelPerPattern: Map<MovementPattern, ExerciseLevel>,
        prescription: Prescription,
        mode: ProgressionMode,
        rawPool: Map<MovementPattern, List<Exercise>>,
        allExercises: List<Exercise>,
    ): PlanDay {
        val templateLetter = MonthlyScheduler.templateFor(sessionIndex)
        val patterns = MonthlyScheduler.TEMPLATE_ROTATION[templateLetter].orEmpty()
        val phase = MonthlyScheduler.phaseFor(day, mode)

        val mainExercises = patterns.mapNotNull { pattern ->
            buildMainExercise(pattern, filteredPool, startingLevelPerPattern[pattern] ?: ExerciseLevel.REGRESSION, prescription, phase.setCount, phase.rpeRange, rawPool, allExercises)
        }

        return PlanDay(
            id = generateId(),
            planId = planId,
            dayNumber = day,
            type = DayType.SESSION,
            templateLetter = templateLetter,
            warmup = MonthlyScheduler.toWarmupBlock(patterns, filteredPool, mandatoryCorrective),
            mainExercises = mainExercises,
            cardio = null,
            cooldown = MonthlyScheduler.toCooldownBlock(patterns, filteredPool, mandatoryCorrective),
            homework = null,
        )
    }

    private fun buildMainExercise(
        pattern: MovementPattern,
        filteredPool: Map<MovementPattern, List<Exercise>>,
        startingLevel: ExerciseLevel,
        prescription: Prescription,
        setCount: Int,
        rpeRange: IntRange,
        rawPool: Map<MovementPattern, List<Exercise>>,
        allExercises: List<Exercise>,
    ): PlannedExercise? {
        val candidates = filteredPool[pattern].orEmpty()
        val chosen = candidates.firstOrNull { it.level == startingLevel }
            ?: candidates.firstOrNull()
            ?: BuildMovementPool.fallbackFor(pattern, allExercises, excludedIds = rawPool[pattern].orEmpty().map { it.id }.toSet()) // POOL-04
            ?: return null // AC-7: caller/UI must surface this gap visibly, never silently

        return PlannedExercise(
            exerciseId = chosen.id,
            sets = setCount,
            repRangeOrDuration = "${prescription.repRange.first}-${prescription.repRange.last} rep",
            rpeTargetMin = rpeRange.first,
            rpeTargetMax = rpeRange.last,
            reasonRuleIds = listOfNotNull("RESEP-${prescription.structureNote}", if (chosen.level != startingLevel) "POOL-04" else null),
        )
    }
}
