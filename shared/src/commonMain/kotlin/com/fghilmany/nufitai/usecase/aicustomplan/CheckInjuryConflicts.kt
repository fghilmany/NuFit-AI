package com.fghilmany.nufitai.usecase.aicustomplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.aicustomplan.entity.InjuryConflict
import com.fghilmany.nufitai.domain.exerciselibrary.repository.ExerciseLibraryRepository
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay

/**
 * AC-5/AC-14 (BLOCKING) -- cross-checks the AI response's exercises against the same
 * `flagExclusion`/`loadedBodyAreas` signals `usecase/monthlyplan/rules/SafetyFilter.kt` (SAFE-01..07,
 * SAFE-10) uses to filter the rule-based tiers' pool, since the AI response bypasses that pipeline
 * entirely. Run this AFTER `ValidateAndFixExerciseIds` -- unknown IDs are already dropped by then.
 */
class CheckInjuryConflicts(private val exerciseLibraryRepository: ExerciseLibraryRepository) {
    suspend operator fun invoke(
        days: List<PlanDay>,
        activeFlags: Set<ExerciseFlag>,
        injuryHistory: Set<BodyArea>,
    ): AppResult<List<InjuryConflict>> {
        val exercisesById = when (val result = exerciseLibraryRepository.getAll()) {
            is AppResult.Success -> result.data.associateBy { it.id }
            is AppResult.Error -> return result
        }

        val conflicts = days.flatMap { day ->
            day.mainExercises.orEmpty().mapNotNull { plannedExercise ->
                val exercise = exercisesById[plannedExercise.exerciseId] ?: return@mapNotNull null
                val flagHit = exercise.flagExclusion.any { it in activeFlags }
                val areaHit = exercise.loadedBodyAreas?.any { it in injuryHistory } == true
                if (flagHit || areaHit) InjuryConflict(day.dayNumber, plannedExercise.exerciseId) else null
            }
        }

        return AppResult.Success(conflicts)
    }
}
