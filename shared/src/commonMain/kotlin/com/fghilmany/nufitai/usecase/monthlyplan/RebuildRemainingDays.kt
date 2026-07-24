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
import com.fghilmany.nufitai.usecase.monthlyplan.rules.BangunKolamGerakan
import com.fghilmany.nufitai.usecase.monthlyplan.rules.FilterKeamanan
import com.fghilmany.nufitai.usecase.monthlyplan.rules.PenjadwalanBulan
import com.fghilmany.nufitai.usecase.monthlyplan.rules.Resep
import com.fghilmany.nufitai.usecase.monthlyplan.rules.ResepGoal

/**
 * AC-10 / Tahap 7B section E: mid-cycle equipment change reruns only layer 2 (POOL) + downstream
 * for days from [fromDayNumber] forward. `startingLevelPerPola` and every prior day's history
 * are preserved untouched -- this only rewrites `plan_day`/`planned_exercise` rows >= [fromDayNumber].
 */
class RebuildRemainingDays(
    private val getExercisePool: GetExercisePool,
    private val repository: MonthlyPlanRepository,
) {
    suspend operator fun invoke(
        plan: MonthlyPlan,
        newPreferensiAlat: Set<EquipmentCategory>,
        fromDayNumber: Int,
        overallLevel: Level,
    ): AppResult<Unit> {
        val poolResult = getExercisePool()
        val allExercises = when (poolResult) {
            is AppResult.Success -> poolResult.data
            is AppResult.Error -> return poolResult
        }

        val rawPool = BangunKolamGerakan(allExercises, newPreferensiAlat, overallLevel)
        val filterResult = FilterKeamanan(rawPool, plan.flagsAktif, areaNyeri = emptySet())
        val resep = ResepGoal(plan.goalMeta)

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
                val templateLetter = PenjadwalanBulan.templateFor(sessionIndex)
                val patterns = PenjadwalanBulan.TEMPLATE_ROTATION[templateLetter].orEmpty()
                val fase = PenjadwalanBulan.faseFor(day, plan.mode)
                val mainExercises = patterns.mapNotNull { pattern ->
                    buildMainExercise(pattern, filterResult.filteredPool, plan.startingLevelPerPola[pattern] ?: ExerciseLevel.REGRESI, resep, fase.setCount, fase.rpeRange)
                }
                PlanDay(
                    id = generateId(),
                    planId = plan.id,
                    dayNumber = day,
                    type = DayType.SESSION,
                    templateLetter = templateLetter,
                    warmup = PenjadwalanBulan.toWarmupBlock(patterns, filterResult.filteredPool, filterResult.korektifWajib),
                    mainExercises = mainExercises,
                    cardio = null,
                    cooldown = PenjadwalanBulan.toCooldownBlock(patterns, filterResult.filteredPool, filterResult.korektifWajib),
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
        resep: Resep,
        setCount: Int,
        rpeRange: IntRange,
    ): PlannedExercise? {
        val chosen = filteredPool[pattern].orEmpty().firstOrNull { it.level == startingLevel }
            ?: filteredPool[pattern].orEmpty().firstOrNull()
            ?: return null
        return PlannedExercise(
            exerciseId = chosen.id,
            sets = setCount,
            repRangeOrDuration = "${resep.repRange.first}-${resep.repRange.last} rep",
            rpeTargetMin = rpeRange.first,
            rpeTargetMax = rpeRange.last,
            reasonRuleIds = listOf("REBUILD-REMAINING-DAYS"),
        )
    }
}
