package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern

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
)
