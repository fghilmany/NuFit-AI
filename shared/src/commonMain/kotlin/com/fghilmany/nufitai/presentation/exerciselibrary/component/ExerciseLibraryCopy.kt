package com.fghilmany.nufitai.presentation.exerciselibrary.component

import androidx.compose.runtime.Composable
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.exerciselibrary_equipment_barbell
import nufitai.shared.generated.resources.exerciselibrary_equipment_bodyweight
import nufitai.shared.generated.resources.exerciselibrary_equipment_cardio
import nufitai.shared.generated.resources.exerciselibrary_equipment_dumbbell
import nufitai.shared.generated.resources.exerciselibrary_equipment_kettlebell
import nufitai.shared.generated.resources.exerciselibrary_equipment_machine
import nufitai.shared.generated.resources.exerciselibrary_equipment_pullupbar
import nufitai.shared.generated.resources.exerciselibrary_level_aksesori
import nufitai.shared.generated.resources.exerciselibrary_level_korektif
import nufitai.shared.generated.resources.exerciselibrary_level_progresi
import nufitai.shared.generated.resources.exerciselibrary_level_regresi
import nufitai.shared.generated.resources.exerciselibrary_level_standar
import nufitai.shared.generated.resources.exerciselibrary_muscle_group_bahu
import nufitai.shared.generated.resources.exerciselibrary_muscle_group_dada
import nufitai.shared.generated.resources.exerciselibrary_muscle_group_glutes
import nufitai.shared.generated.resources.exerciselibrary_muscle_group_inti
import nufitai.shared.generated.resources.exerciselibrary_muscle_group_kaki
import nufitai.shared.generated.resources.exerciselibrary_muscle_group_kardio
import nufitai.shared.generated.resources.exerciselibrary_muscle_group_lengan
import nufitai.shared.generated.resources.exerciselibrary_muscle_group_punggung
import org.jetbrains.compose.resources.stringResource

@Composable
fun MuscleGroup.shortLabel(): String = when (this) {
    MuscleGroup.LEG -> stringResource(Res.string.exerciselibrary_muscle_group_kaki)
    MuscleGroup.CHEST -> stringResource(Res.string.exerciselibrary_muscle_group_dada)
    MuscleGroup.BACK -> stringResource(Res.string.exerciselibrary_muscle_group_punggung)
    MuscleGroup.SHOULDER -> stringResource(Res.string.exerciselibrary_muscle_group_bahu)
    MuscleGroup.ARM -> stringResource(Res.string.exerciselibrary_muscle_group_lengan)
    MuscleGroup.CORE -> stringResource(Res.string.exerciselibrary_muscle_group_inti)
    MuscleGroup.GLUTES -> stringResource(Res.string.exerciselibrary_muscle_group_glutes)
    MuscleGroup.CARDIO -> stringResource(Res.string.exerciselibrary_muscle_group_kardio)
}

/** issue #79 §5 level-display decision: REGRESSION/STANDARD/PROGRESSION shown; CORRECTIVE/ACCESSORY excluded from browse. */
@Composable
fun ExerciseLevel.shortLabel(): String = when (this) {
    ExerciseLevel.REGRESSION -> stringResource(Res.string.exerciselibrary_level_regresi)
    ExerciseLevel.STANDARD -> stringResource(Res.string.exerciselibrary_level_standar)
    ExerciseLevel.PROGRESSION -> stringResource(Res.string.exerciselibrary_level_progresi)
    ExerciseLevel.CORRECTIVE -> stringResource(Res.string.exerciselibrary_level_korektif)
    ExerciseLevel.ACCESSORY -> stringResource(Res.string.exerciselibrary_level_aksesori)
}

@Composable
fun EquipmentCategory.shortLabel(): String = when (this) {
    EquipmentCategory.BODYWEIGHT -> stringResource(Res.string.exerciselibrary_equipment_bodyweight)
    EquipmentCategory.DUMBBELL -> stringResource(Res.string.exerciselibrary_equipment_dumbbell)
    EquipmentCategory.BARBELL -> stringResource(Res.string.exerciselibrary_equipment_barbell)
    EquipmentCategory.KETTLEBELL -> stringResource(Res.string.exerciselibrary_equipment_kettlebell)
    EquipmentCategory.MACHINE_CABLE -> stringResource(Res.string.exerciselibrary_equipment_machine)
    EquipmentCategory.PULL_UP_BAR -> stringResource(Res.string.exerciselibrary_equipment_pullupbar)
    EquipmentCategory.CARDIO_EQUIPMENT -> stringResource(Res.string.exerciselibrary_equipment_cardio)
}

/** Browsable levels only -- CORRECTIVE/ACCESSORY are excluded from P-07's Level filter (issue #79 §5). */
val BrowsableExerciseLevels: List<ExerciseLevel> = listOf(ExerciseLevel.REGRESSION, ExerciseLevel.STANDARD, ExerciseLevel.PROGRESSION)
