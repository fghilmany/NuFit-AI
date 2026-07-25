package com.fghilmany.nufitai.data.exerciselibrary.model

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup
import com.fghilmany.nufitai.domain.exerciselibrary.entity.toExerciseFlagOrNull
import kotlinx.serialization.Serializable

/**
 * Mirrors the schema of the bundled `files/exercises.json` asset (extracted from
 * `Gym Techniques/`, see `docs/data/exercises-seed-gaps.md` for extraction notes).
 * Unknown/unmapped raw flag tokens are silently dropped via `toExerciseFlagOrNull()` --
 * the extraction pass already validated every token against the issue #29 taxonomy.
 */
@Serializable
data class ExerciseSeedDto(
    val id: String,
    val name: String,
    val equipmentCategory: String,
    val movementPattern: String,
    val level: String,
    val levelVariant: Int? = null,
    val levelNote: String? = null,
    val flagExclusion: List<String> = emptyList(),
    val flagPrioritas: List<String> = emptyList(),
    val areaTerbebani: List<String>? = null,
    val substitusiSetara: Map<String, String>? = null,
    val rantaiRegresi: String? = null,
    val rantaiProgresi: String? = null,
    val syaratNaik: String? = null,
    val polaGerakTerkait: List<String>? = null,
    val highImpact: Boolean = false,
    val isometricHeavy: Boolean = false,
    val mediaSlug: String? = null,
    val primaryMuscleGroup: String,
    val targetMusclesPrimary: List<String> = emptyList(),
    val targetMusclesSecondary: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val commonMistakes: List<CommonMistakeJson> = emptyList(),
    val safetyTips: List<String> = emptyList(),
) {
    fun toEntity(): Exercise = Exercise(
        id = id,
        name = name,
        equipmentCategory = EquipmentCategory.valueOf(equipmentCategory),
        movementPattern = MovementPattern.valueOf(movementPattern),
        level = ExerciseLevel.valueOf(level),
        levelVariant = levelVariant,
        levelNote = levelNote,
        flagExclusion = flagExclusion.mapNotNull { it.toExerciseFlagOrNull() }.toSet(),
        flagPrioritas = flagPrioritas.mapNotNull { it.toExerciseFlagOrNull() }.toSet(),
        areaTerbebani = areaTerbebani?.map { BodyArea.valueOf(it.uppercase()) }?.toSet(),
        substitusiSetara = substitusiSetara
            ?.mapNotNull { (key, value) -> key.toEquipmentCategoryOrNull()?.let { it to value } }
            ?.toMap()
            ?.takeIf { it.isNotEmpty() },
        rantaiRegresi = rantaiRegresi,
        rantaiProgresi = rantaiProgresi,
        syaratNaik = syaratNaik,
        polaGerakTerkait = polaGerakTerkait?.map { MovementPattern.valueOf(it) }?.toSet(),
        highImpact = highImpact,
        isometricHeavy = isometricHeavy,
        mediaSlug = mediaSlug,
        primaryMuscleGroup = MuscleGroup.valueOf(primaryMuscleGroup),
        targetMusclesPrimary = targetMusclesPrimary,
        targetMusclesSecondary = targetMusclesSecondary,
        instructions = instructions,
        commonMistakes = commonMistakes.map { it.toEntity() },
        safetyTips = safetyTips,
    )
}

/**
 * `substitusiSetara` keys in the source JSON are free-text Indonesian/English category
 * words ("mesin", "band", "dumbbell", "bodyweight"), not [EquipmentCategory] enum names.
 * "band" (Resistance Band) has no exercise database (ADR-002 decision 1) and never
 * resolves -- dropped rather than crashing, consistent with ADR-002's "never guess" rule.
 */
private fun String.toEquipmentCategoryOrNull(): EquipmentCategory? = when (lowercase()) {
    "bodyweight" -> EquipmentCategory.BODYWEIGHT
    "dumbbell" -> EquipmentCategory.DUMBBELL
    "barbell" -> EquipmentCategory.BARBELL
    "kettlebell" -> EquipmentCategory.KETTLEBELL
    "mesin", "cable", "machine" -> EquipmentCategory.MACHINE_CABLE
    "pull_up_bar", "pull-up bar", "pullup" -> EquipmentCategory.PULL_UP_BAR
    "cardio" -> EquipmentCategory.CARDIO_EQUIPMENT
    else -> null
}
