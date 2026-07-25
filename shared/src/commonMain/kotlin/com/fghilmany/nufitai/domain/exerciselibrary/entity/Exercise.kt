package com.fghilmany.nufitai.domain.exerciselibrary.entity

/**
 * One row from the `Gym Techniques` catalog (id matches the source catalog id verbatim,
 * e.g. "BW-SQUAT-000"). Seeded at first launch from the bundled JSON asset -- see
 * `data/exerciselibrary/datasource/ExerciseLibrarySeedDataSource.kt`. Not user-writable.
 */
data class Exercise(
    val id: String,
    val name: String,
    val equipmentCategory: EquipmentCategory,
    val movementPattern: MovementPattern,
    val level: ExerciseLevel,
    val levelVariant: Int?,
    val levelNote: String?,
    val flagExclusion: Set<ExerciseFlag>,
    val flagPriority: Set<ExerciseFlag>,
    val loadedBodyAreas: Set<BodyArea>?,
    val equivalentSubstitutes: Map<EquipmentCategory, String>?,
    val regressionChain: String?,
    val progressionChain: String?,
    val levelUpRequirement: String?,
    val relatedMovementPatterns: Set<MovementPattern>?,
    val highImpact: Boolean,
    val isometricHeavy: Boolean,
    val mediaSlug: String?,
    /** issue #79 -- P-07 card badge + "Otot Target" filter (coarse, closed set). */
    val primaryMuscleGroup: MuscleGroup,
    /** issue #79 -- P-08 target-muscle chip row, Indonesian display labels. */
    val targetMusclesPrimary: List<String>,
    val targetMusclesSecondary: List<String>,
    /** issue #79 -- P-08 "Instruksi Langkah-demi-Langkah", ordered. */
    val instructions: List<String>,
    /** issue #79 -- P-08 "Hindari Hal Ini". Never empty (AC-5, BLOCKING). */
    val commonMistakes: List<CommonMistake>,
    /** issue #79 -- P-08 "Saran NuFit AI". Never empty (AC-5, BLOCKING). */
    val safetyTips: List<String>,
)
