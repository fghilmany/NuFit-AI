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
    val flagPrioritas: Set<ExerciseFlag>,
    val areaTerbebani: Set<BodyArea>?,
    val substitusiSetara: Map<EquipmentCategory, String>?,
    val rantaiRegresi: String?,
    val rantaiProgresi: String?,
    val syaratNaik: String?,
    val polaGerakTerkait: Set<MovementPattern>?,
    val highImpact: Boolean,
    val isometricHeavy: Boolean,
    val mediaSlug: String?,
)
