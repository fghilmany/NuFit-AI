package com.fghilmany.nufitai.usecase.aicustomplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.aicustomplan.entity.ExerciseValidationWarning
import com.fghilmany.nufitai.domain.aicustomplan.entity.ValidatedPlanDraft
import com.fghilmany.nufitai.domain.exerciselibrary.repository.ExerciseLibraryRepository
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay

/**
 * AC-4 (BLOCKING). An unknown `exerciseId` has no library entry to read a movement pattern
 * from, so there is nothing to match a substitute against (dev decision, techspec §9 item 10) --
 * the slot is dropped and surfaced as a warning, never silently omitted.
 */
class ValidateAndFixExerciseIds(private val exerciseLibraryRepository: ExerciseLibraryRepository) {
    suspend operator fun invoke(days: List<PlanDay>): AppResult<ValidatedPlanDraft> {
        val validIds = when (val result = exerciseLibraryRepository.getAll()) {
            is AppResult.Success -> result.data.map { it.id }.toSet()
            is AppResult.Error -> return result
        }

        val warnings = mutableListOf<ExerciseValidationWarning>()
        val fixedDays = days.map { day ->
            val mainExercises = day.mainExercises?.filter { plannedExercise ->
                val isValid = plannedExercise.exerciseId in validIds
                if (!isValid) warnings += ExerciseValidationWarning(day.dayNumber, plannedExercise.exerciseId)
                isValid
            }
            day.copy(mainExercises = mainExercises)
        }

        return AppResult.Success(ValidatedPlanDraft(fixedDays, warnings))
    }
}
