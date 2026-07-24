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
import com.fghilmany.nufitai.usecase.monthlyplan.rules.BangunKolamGerakan
import com.fghilmany.nufitai.usecase.monthlyplan.rules.FilterKeamanan
import com.fghilmany.nufitai.usecase.monthlyplan.rules.GatingProgresi
import com.fghilmany.nufitai.usecase.monthlyplan.rules.KalibrasiStartingLevel
import com.fghilmany.nufitai.usecase.monthlyplan.rules.PenjadwalanBulan
import com.fghilmany.nufitai.usecase.monthlyplan.rules.Resep
import com.fghilmany.nufitai.usecase.monthlyplan.rules.ResepGoal

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
 * categorization always resolves to `null` (see `KalibrasiStartingLevel`'s doc: norm tables
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

        val rawPool = BangunKolamGerakan(allExercises, fullAssessment.preferensiAlat, overallLevel)
        val activeFlags = fullAssessment.parQKategoriB + fullAssessment.flagsPostural + fullAssessment.flagsGerak
        val filterResult = FilterKeamanan(rawPool, activeFlags, fullAssessment.riwayatCedera)
        val resep = ResepGoal(fullAssessment.goal)

        // PROG-06: conservative mode when GATE-02 fired (any parQKategoriB flag) OR flagsAktif >= 3.
        val mode = if (fullAssessment.parQKategoriB.isNotEmpty() || activeFlags.size >= 3) ProgressionMode.KONSERVATIF else ProgressionMode.NORMAL
        val checkpointDays = GatingProgresi.checkpointDays(mode)

        val startingLevelPerPola = STARTING_LEVEL_PATTERNS.associateWith { pattern ->
            val hasExclusion = (filterResult.filteredPool[pattern]?.size ?: 0) < (rawPool[pattern]?.size ?: 0) // CAL-01 proxy
            KalibrasiStartingLevel(
                hasExclusionFlagForPattern = hasExclusion,
                hasDirectTest = pattern in DIRECT_TEST_PATTERNS,
                scoreCategory = null, // norm tables unavailable -- see class doc
                level = overallLevel,
            )
        }

        val planId = generateId()
        val sessionDays = PenjadwalanBulan.buildKerangkaKalender(
            frekuensiPerMinggu = fullAssessment.frekuensiPerMinggu,
            hariPilihan = fullAssessment.hariPilihan,
            minRestBetweenSessions = overallLevel == Level.BEGINNER,
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
            flagsAktif = activeFlags,
            startingLevelPerPola = startingLevelPerPola,
            mode = mode,
            checkpointDays = checkpointDays,
        )

        val days = (1..30).map { day ->
            val sessionIndex = sessionDays.indexOf(day)
            if (sessionIndex == -1) {
                PlanDay(generateId(), planId, day, DayType.REST, null, null, null, null, null, null)
            } else {
                buildSessionDay(day, sessionIndex, planId, filterResult.filteredPool, filterResult.korektifWajib, startingLevelPerPola, resep, mode, rawPool = rawPool, allExercises = allExercises)
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
        korektifWajib: List<Exercise>,
        startingLevelPerPola: Map<MovementPattern, ExerciseLevel>,
        resep: Resep,
        mode: ProgressionMode,
        rawPool: Map<MovementPattern, List<Exercise>>,
        allExercises: List<Exercise>,
    ): PlanDay {
        val templateLetter = PenjadwalanBulan.templateFor(sessionIndex)
        val patterns = PenjadwalanBulan.TEMPLATE_ROTATION[templateLetter].orEmpty()
        val fase = PenjadwalanBulan.faseFor(day, mode)

        val mainExercises = patterns.mapNotNull { pattern ->
            buildMainExercise(pattern, filteredPool, startingLevelPerPola[pattern] ?: ExerciseLevel.REGRESI, resep, fase.setCount, fase.rpeRange, rawPool, allExercises)
        }

        return PlanDay(
            id = generateId(),
            planId = planId,
            dayNumber = day,
            type = DayType.SESSION,
            templateLetter = templateLetter,
            warmup = PenjadwalanBulan.toWarmupBlock(patterns, filteredPool, korektifWajib),
            mainExercises = mainExercises,
            cardio = null,
            cooldown = PenjadwalanBulan.toCooldownBlock(patterns, filteredPool, korektifWajib),
            homework = null,
        )
    }

    private fun buildMainExercise(
        pattern: MovementPattern,
        filteredPool: Map<MovementPattern, List<Exercise>>,
        startingLevel: ExerciseLevel,
        resep: Resep,
        setCount: Int,
        rpeRange: IntRange,
        rawPool: Map<MovementPattern, List<Exercise>>,
        allExercises: List<Exercise>,
    ): PlannedExercise? {
        val candidates = filteredPool[pattern].orEmpty()
        val chosen = candidates.firstOrNull { it.level == startingLevel }
            ?: candidates.firstOrNull()
            ?: BangunKolamGerakan.fallbackFor(pattern, allExercises, excludedIds = rawPool[pattern].orEmpty().map { it.id }.toSet()) // POOL-04
            ?: return null // AC-7: caller/UI must surface this gap visibly, never silently

        return PlannedExercise(
            exerciseId = chosen.id,
            sets = setCount,
            repRangeOrDuration = "${resep.repRange.first}-${resep.repRange.last} rep",
            rpeTargetMin = rpeRange.first,
            rpeTargetMax = rpeRange.last,
            reasonRuleIds = listOfNotNull("RESEP-${resep.struktur}", if (chosen.level != startingLevel) "POOL-04" else null),
        )
    }
}
