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
import com.fghilmany.nufitai.domain.onboarding.entity.FrequencyBucket
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import com.fghilmany.nufitai.usecase.monthlyplan.rules.BangunKolamGerakan
import com.fghilmany.nufitai.usecase.monthlyplan.rules.PenjadwalanBulan
import com.fghilmany.nufitai.usecase.monthlyplan.rules.Resep
import com.fghilmany.nufitai.usecase.monthlyplan.rules.ResepGoal

private val UPPER_PATTERNS = listOf(MovementPattern.PUSH_HORIZONTAL, MovementPattern.PUSH_VERTICAL, MovementPattern.PULL_HORIZONTAL, MovementPattern.CORE)
private val LOWER_PATTERNS = listOf(MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.LUNGE, MovementPattern.CORE)

/**
 * Local tier: template matrix (00-overview.md Keputusan #3), reuses `QuickAssessmentResult`.
 * Skips GATE (already applied at Quick Assessment time)/SAFETY (no postural/movement flags
 * collected)/CALIBRATION (no capacity test)/PROGRESSION-GATING (fixed fase table only) --
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

        val pool = BangunKolamGerakan(allExercises, quickAssessment.input.equipment, quickAssessment.level)
        val startingLevel = if (quickAssessment.level == Level.BEGINNER) ExerciseLevel.REGRESI else ExerciseLevel.STANDAR
        val resep = ResepGoal(quickAssessment.input.goal)

        val sessionsPerWeek = when (quickAssessment.input.frequency) {
            FrequencyBucket.TWO_TO_THREE -> 3
            FrequencyBucket.FOUR_TO_FIVE -> 4
        }
        val hariPilihan = evenlySpreadWeekdays(sessionsPerWeek)
        val sessionDays = PenjadwalanBulan.buildKerangkaKalender(sessionsPerWeek, hariPilihan, minRestBetweenSessions = true)

        val planId = generateId()
        val startingLevelPerPola = MovementPattern.entries
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
            flagsAktif = emptySet(),
            startingLevelPerPola = startingLevelPerPola,
            mode = ProgressionMode.NORMAL,
            checkpointDays = emptyList(), // Local tier: no progression gating, fixed fase table only
        )

        val patternsForSplit = if (quickAssessment.resolvedSplit == ResolvedSplit.FULL_BODY) {
            List(sessionDays.size) { PenjadwalanBulan.templateFor(it) to (PenjadwalanBulan.TEMPLATE_ROTATION[PenjadwalanBulan.templateFor(it)] ?: emptyList()) }
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
                val fase = PenjadwalanBulan.faseFor(day, ProgressionMode.NORMAL)
                val mainExercises = patterns.mapNotNull { pattern -> buildMainExercise(pattern, pool, startingLevel, resep, fase.setCount, fase.rpeRange) }
                PlanDay(
                    id = generateId(),
                    planId = planId,
                    dayNumber = day,
                    type = DayType.SESSION,
                    templateLetter = templateLetter,
                    warmup = PenjadwalanBulan.toWarmupBlock(patterns, pool, korektifWajib = emptyList()),
                    mainExercises = mainExercises,
                    cardio = null,
                    cooldown = PenjadwalanBulan.toCooldownBlock(patterns, pool, korektifWajib = emptyList()),
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
        resep: Resep,
        setCount: Int,
        rpeRange: IntRange,
    ): PlannedExercise? {
        val candidates = pool[pattern].orEmpty()
        val chosen = candidates.firstOrNull { it.level == startingLevel } ?: candidates.firstOrNull() ?: return null
        return PlannedExercise(
            exerciseId = chosen.id,
            sets = setCount,
            repRangeOrDuration = "${resep.repRange.first}-${resep.repRange.last} rep",
            rpeTargetMin = rpeRange.first,
            rpeTargetMax = rpeRange.last,
            reasonRuleIds = listOf("LOCAL-TEMPLATE"),
        )
    }
}

private fun evenlySpreadWeekdays(sessionsPerWeek: Int): Set<Int> = when (sessionsPerWeek) {
    3 -> setOf(1, 3, 5) // Mon/Wed/Fri
    4 -> setOf(1, 2, 4, 5) // Mon/Tue/Thu/Fri
    else -> setOf(1, 3, 5)
}
