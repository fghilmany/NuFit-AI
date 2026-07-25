package com.fghilmany.nufitai.presentation.exerciselibrary.component

import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup

fun MuscleGroup.shortLabel(): String = when (this) {
    MuscleGroup.KAKI -> "Kaki"
    MuscleGroup.DADA -> "Dada"
    MuscleGroup.PUNGGUNG -> "Punggung"
    MuscleGroup.BAHU -> "Bahu"
    MuscleGroup.LENGAN -> "Lengan"
    MuscleGroup.INTI -> "Inti"
    MuscleGroup.GLUTES -> "Glutes"
    MuscleGroup.KARDIO -> "Kardio"
}

/** issue #79 §5 level-display decision: REGRESI/STANDAR/PROGRESI shown; KOREKTIF/AKSESORI excluded from browse. */
fun ExerciseLevel.shortLabel(): String = when (this) {
    ExerciseLevel.REGRESI -> "Pemula"
    ExerciseLevel.STANDAR -> "Menengah"
    ExerciseLevel.PROGRESI -> "Lanjutan"
    ExerciseLevel.KOREKTIF -> "Korektif"
    ExerciseLevel.AKSESORI -> "Aksesori"
}

fun EquipmentCategory.shortLabel(): String = when (this) {
    EquipmentCategory.BODYWEIGHT -> "Bodyweight"
    EquipmentCategory.DUMBBELL -> "Dumbbell"
    EquipmentCategory.BARBELL -> "Barbell"
    EquipmentCategory.KETTLEBELL -> "Kettlebell"
    EquipmentCategory.MACHINE_CABLE -> "Machine"
    EquipmentCategory.PULL_UP_BAR -> "Pull-Up Bar"
    EquipmentCategory.CARDIO_EQUIPMENT -> "Cardio"
}

/** Browsable levels only -- KOREKTIF/AKSESORI are excluded from P-07's Level filter (issue #79 §5). */
val BrowsableExerciseLevels: List<ExerciseLevel> = listOf(ExerciseLevel.REGRESI, ExerciseLevel.STANDAR, ExerciseLevel.PROGRESI)
