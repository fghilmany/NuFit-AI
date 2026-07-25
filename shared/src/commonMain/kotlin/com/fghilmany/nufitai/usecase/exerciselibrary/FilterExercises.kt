package com.fghilmany.nufitai.usecase.exerciselibrary

import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup

/** Empty set in any facet means "no filter applied" for that facet (issue #79 §9 item 7). */
data class ExerciseFilter(
    val muscleGroups: Set<MuscleGroup> = emptySet(),
    val equipment: Set<EquipmentCategory> = emptySet(),
    val levels: Set<ExerciseLevel> = emptySet(),
)

/**
 * P-07 search + filter (AC-1) -- pure, in-memory over an already-fetched pool (135 exercises,
 * no DB-level query needed). AND across otot/alat/level facets, OR within a facet.
 *
 * CORRECTIVE/ACCESSORY exercises are excluded from browse entirely (§5 level-display decision) --
 * they're plan-generation-only building blocks, not user-facing techniques. This exclusion is
 * unconditional, not a `filter.levels` option, so it can never be filtered back in.
 */
class FilterExercises {
    operator fun invoke(pool: List<Exercise>, query: String, filter: ExerciseFilter): List<Exercise> = pool
        .filter { it.level != ExerciseLevel.CORRECTIVE && it.level != ExerciseLevel.ACCESSORY }
        .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
        .filter { filter.muscleGroups.isEmpty() || it.primaryMuscleGroup in filter.muscleGroups }
        .filter { filter.equipment.isEmpty() || it.equipmentCategory in filter.equipment }
        .filter { filter.levels.isEmpty() || it.level in filter.levels }
}
