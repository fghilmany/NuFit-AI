package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.CommonMistake
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup

fun testExercise(
    id: String,
    equipmentCategory: EquipmentCategory = EquipmentCategory.BODYWEIGHT,
    movementPattern: MovementPattern = MovementPattern.SQUAT,
    level: ExerciseLevel = ExerciseLevel.STANDAR,
    levelVariant: Int? = null,
    flagExclusion: Set<ExerciseFlag> = emptySet(),
    flagPrioritas: Set<ExerciseFlag> = emptySet(),
    areaTerbebani: Set<BodyArea>? = null,
    substitusiSetara: Map<EquipmentCategory, String>? = null,
    polaGerakTerkait: Set<MovementPattern>? = null,
    primaryMuscleGroup: MuscleGroup = MuscleGroup.KAKI,
    targetMusclesPrimary: List<String> = emptyList(),
    targetMusclesSecondary: List<String> = emptyList(),
    instructions: List<String> = listOf("Langkah 1"),
    commonMistakes: List<CommonMistake> = listOf(CommonMistake("Kesalahan", "Deskripsi")),
    safetyTips: List<String> = listOf("Tips"),
): Exercise = Exercise(
    id = id,
    name = id,
    equipmentCategory = equipmentCategory,
    movementPattern = movementPattern,
    level = level,
    levelVariant = levelVariant,
    levelNote = null,
    flagExclusion = flagExclusion,
    flagPrioritas = flagPrioritas,
    areaTerbebani = areaTerbebani,
    substitusiSetara = substitusiSetara,
    rantaiRegresi = null,
    rantaiProgresi = null,
    syaratNaik = null,
    polaGerakTerkait = polaGerakTerkait,
    highImpact = false,
    isometricHeavy = false,
    mediaSlug = null,
    primaryMuscleGroup = primaryMuscleGroup,
    targetMusclesPrimary = targetMusclesPrimary,
    targetMusclesSecondary = targetMusclesSecondary,
    instructions = instructions,
    commonMistakes = commonMistakes,
    safetyTips = safetyTips,
)
