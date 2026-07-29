package com.fghilmany.nufitai.domain.exerciselibrary.entity

/**
 * ADR-007 -- data-source pivot: one row keyed by the free-exercise-db numeric id (e.g. "0001"),
 * enriched with the KMP-specific fields (equipmentCategory, movementPattern, flags, etc., still
 * sourced from `Gym Techniques`). Seeded at first launch from the bundled JSON asset -- see
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
    /**
     * ADR-007 -- keys are no longer equipment categories: `"duplicate_variant"` (id of an
     * equivalent exercise using different equipment), `"bodyweight_alt"` (id of a bodyweight-only
     * version), `"note"` (free text, NOT an exerciseId -- exclude before resolving ids, see
     * `GetExerciseAlternatives`).
     */
    val equivalentSubstitutes: Map<String, String>?,
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
    /** issue #79 -- P-08 "Instruksi Langkah-demi-Langkah", ordered. Indonesian (ADR-007 -- LLM-translated from the source's English steps, no human review yet). */
    val instructions: List<String>,
    /** issue #79 -- P-08 "Hindari Hal Ini". Never empty (AC-5, BLOCKING). */
    val commonMistakes: List<CommonMistake>,
    /** issue #79 -- P-08 "Saran NuFit AI". Never empty (AC-5, BLOCKING). */
    val safetyTips: List<String>,
    /** ADR-007 -- resource-relative path (e.g. "files/images/0001-xxx.jpg"), read via Res.readBytes(). */
    val imagePath: String?,
    /** ADR-007 -- animated GIF, same resource-path convention as [imagePath]. */
    val gifPath: String?,
)
