package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.onboarding.entity.Level

/** issue #29 layer 2 -- POOL-01..05. Pure, no I/O. */
object BuildMovementPool {

    /**
     * POOL-01/02/03: filter by equipment preference (bodyweight always included), group by
     * pattern, sort each group regression->standard->progression.
     * POOL-05: Beginner priority -- Barbell deferred out of the first-cycle pool entirely
     * (per issue #29: "meski dicentang"); Kettlebell/Pull-up Bar deferral (post-checkpoint-14
     * unlock) is NOT modeled yet -- documented gap, see class doc below.
     */
    operator fun invoke(
        allExercises: List<Exercise>,
        equipmentPreference: Set<EquipmentCategory>,
        level: Level,
    ): Map<MovementPattern, List<Exercise>> {
        val effectiveEquipment = equipmentPreference + EquipmentCategory.BODYWEIGHT // POOL-02
        val allowedEquipment = if (level == Level.BEGINNER) {
            effectiveEquipment - EquipmentCategory.BARBELL // POOL-05: Barbell deferred for beginners
        } else {
            effectiveEquipment
        }

        return allExercises
            .filter { it.equipmentCategory in allowedEquipment }
            .groupBy { it.movementPattern }
            .mapValues { (_, exercises) -> exercises.sortedWith(levelOrder) }
    }

    /**
     * POOL-04: fallback when a pattern's pool (post-SAFETY-filter, see `SafetyFilter`) is
     * empty -- try `equivalentSubstitutes` from any exercise in the ORIGINAL unfiltered same-pattern
     * pool (before equipment filtering), across equipment categories. Returns null if truly
     * nothing is available (caller must surface a visible note to the user, never a silent gap
     * -- AC-7, DoD "BLOCKING").
     */
    fun fallbackFor(
        pattern: MovementPattern,
        allExercises: List<Exercise>,
        excludedIds: Set<String>,
    ): Exercise? {
        val candidates = allExercises.filter { it.movementPattern == pattern && it.id !in excludedIds }
        val substituteId = candidates.firstNotNullOfOrNull { it.equivalentSubstitutes?.values?.firstOrNull() }
        return substituteId?.let { id -> allExercises.find { it.id == id } }
    }

    private val levelOrder = compareBy<Exercise>(
        { it.level.progressionRank },
        { it.levelVariant ?: 0 },
    )
}

/** Enum declaration order isn't progression order (REGRESSION, STANDARD, PROGRESSION, CORRECTIVE, ACCESSORY) -- this is. */
private val ExerciseLevel.progressionRank: Int
    get() = when (this) {
        ExerciseLevel.REGRESSION -> 0
        ExerciseLevel.CORRECTIVE -> 0
        ExerciseLevel.STANDARD -> 1
        ExerciseLevel.ACCESSORY -> 1
        ExerciseLevel.PROGRESSION -> 2
    }
