package com.fghilmany.nufitai.usecase.exerciselibrary

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.exerciselibrary.repository.ExerciseLibraryRepository

private const val MAX_ALTERNATIVES = 4

/**
 * P-08 "Gerakan Alternatif" (issue #79 §5) -- computed heuristic, not hand-curated (the issue's
 * own DoD flags this relation as previously undefined):
 * 1. Cross-equipment: resolve `substitusiSetara` (already exists, EquipmentCategory -> exerciseId)
 * 2. Same-equipment variant: siblings sharing movementPattern + equipmentCategory, excluding
 *    self and excluding CORRECTIVE/STRETCH patterns or KOREKTIF/AKSESORI levels (not
 *    user-facing techniques -- same exclusion as [com.fghilmany.nufitai.usecase.exerciselibrary.FilterExercises])
 * Capped at 4 (Figma shows 2; the cap is a safety ceiling, not a target).
 */
class GetExerciseAlternatives(private val repository: ExerciseLibraryRepository) {
    suspend operator fun invoke(exercise: Exercise): AppResult<List<Exercise>> {
        val allExercises = when (val result = repository.getAll()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        val byId = allExercises.associateBy { it.id }

        val crossEquipment = exercise.substitusiSetara?.values.orEmpty().mapNotNull { byId[it] }

        val siblings = allExercises.filter { candidate ->
            candidate.id != exercise.id &&
                candidate.movementPattern == exercise.movementPattern &&
                candidate.equipmentCategory == exercise.equipmentCategory &&
                candidate.movementPattern != MovementPattern.CORRECTIVE &&
                candidate.movementPattern != MovementPattern.STRETCH &&
                candidate.level != ExerciseLevel.KOREKTIF &&
                candidate.level != ExerciseLevel.AKSESORI
        }

        val alternatives = (crossEquipment + siblings)
            .distinctBy { it.id }
            .filter { it.id != exercise.id }
            .take(MAX_ALTERNATIVES)

        return AppResult.Success(alternatives)
    }
}
