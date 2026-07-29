package com.fghilmany.nufitai.data.exerciselibrary.model

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup
import com.fghilmany.nufitai.domain.exerciselibrary.entity.toExerciseFlagOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val LANG_ID = "id"
private const val LANG_EN = "en"

/**
 * ADR-007 -- mirrors the bundled `files/exercises.json` asset's current shape: free-exercise-db
 * fields (id, name, image, gif_url, plus raw category/body_part/equipment/muscle_group/target
 * which are unused and dropped via `ignoreUnknownKeys`) merged with the KMP-specific enrichment
 * fields still sourced from `Gym Techniques` (equipmentCategory, movementPattern, flags, etc.).
 * `instruction_steps`/`commonMistakes`/`safetyTips`/`levelUpRequirement` are language-keyed
 * maps; "id" preferred, falling back to "en" (instruction_steps' "id" key is LLM-translated
 * from the source's English steps, not human-reviewed yet -- see ADR-007).
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
    val flagPriority: List<String> = emptyList(),
    val loadedBodyAreas: List<String>? = null,
    val equivalentSubstitutes: Map<String, String>? = null,
    val regressionChain: String? = null,
    val progressionChain: String? = null,
    val levelUpRequirement: Map<String, String>? = null,
    val relatedMovementPatterns: List<String>? = null,
    val highImpact: Boolean = false,
    val isometricHeavy: Boolean = false,
    val mediaSlug: String? = null,
    val primaryMuscleGroup: String,
    val targetMusclesPrimary: List<String> = emptyList(),
    val targetMusclesSecondary: List<String> = emptyList(),
    @SerialName("instruction_steps") val instructionSteps: Map<String, List<String>> = emptyMap(),
    val commonMistakes: Map<String, List<CommonMistakeJson>> = emptyMap(),
    val safetyTips: Map<String, List<String>> = emptyMap(),
    val image: String? = null,
    @SerialName("gif_url") val gifUrl: String? = null,
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
        flagPriority = flagPriority.mapNotNull { it.toExerciseFlagOrNull() }.toSet(),
        loadedBodyAreas = loadedBodyAreas?.map { BodyArea.valueOf(it.uppercase()) }?.toSet(),
        equivalentSubstitutes = equivalentSubstitutes,
        regressionChain = regressionChain,
        progressionChain = progressionChain,
        levelUpRequirement = levelUpRequirement?.preferIdThenEn(),
        relatedMovementPatterns = relatedMovementPatterns?.map { MovementPattern.valueOf(it) }?.toSet(),
        highImpact = highImpact,
        isometricHeavy = isometricHeavy,
        mediaSlug = mediaSlug,
        primaryMuscleGroup = MuscleGroup.valueOf(primaryMuscleGroup),
        targetMusclesPrimary = targetMusclesPrimary,
        targetMusclesSecondary = targetMusclesSecondary,
        instructions = instructionSteps[LANG_ID] ?: instructionSteps[LANG_EN] ?: emptyList(),
        commonMistakes = (commonMistakes[LANG_ID] ?: commonMistakes[LANG_EN] ?: emptyList()).map { it.toEntity() },
        safetyTips = safetyTips[LANG_ID] ?: safetyTips[LANG_EN] ?: emptyList(),
        imagePath = image?.let { "files/$it" },
        gifPath = gifUrl?.let { "files/$it" },
    )

    private fun Map<String, String>.preferIdThenEn(): String? = get(LANG_ID) ?: get(LANG_EN)
}
