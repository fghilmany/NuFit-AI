package com.fghilmany.nufitai.presentation.fullassessment.component

import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag

/** Plain-language labels for the postural/movement screening checklists (stub scope). */
fun ExerciseFlag.shortLabel(): String = when (this) {
    ExerciseFlag.HEALTH_JANTUNG -> "Jantung"
    ExerciseFlag.HEALTH_TEKANAN_DARAH -> "Tekanan darah"
    ExerciseFlag.HEALTH_DIABETES -> "Diabetes"
    ExerciseFlag.HEALTH_ASMA -> "Asma"
    ExerciseFlag.HEALTH_SENDI -> "Sendi"
    ExerciseFlag.HEALTH_OSTEOPOROSIS -> "Osteoporosis"
    ExerciseFlag.POSTURAL_FORWARD_HEAD -> "Kepala maju (forward head)"
    ExerciseFlag.POSTURAL_ROUNDED_SHOULDER -> "Bahu membulat ke depan"
    ExerciseFlag.POSTURAL_KYPHOSIS -> "Kyphosis (punggung atas membulat)"
    ExerciseFlag.POSTURAL_APT -> "Anterior pelvic tilt"
    ExerciseFlag.POSTURAL_PPT -> "Posterior pelvic tilt"
    ExerciseFlag.POSTURAL_ASIMETRI_BAHU -> "Asimetri bahu"
    ExerciseFlag.POSTURAL_ASIMETRI_PINGGUL -> "Asimetri pinggul"
    ExerciseFlag.MOVEMENT_ANKLE_MOBILITY -> "Mobilitas pergelangan kaki terbatas"
    ExerciseFlag.MOVEMENT_KNEE_VALGUS -> "Lutut masuk ke dalam saat squat (knee valgus)"
    ExerciseFlag.MOVEMENT_HINGE_FROM_BACK -> "Hip hinge dari punggung, bukan pinggul"
    ExerciseFlag.MOVEMENT_SHOULDER_MOBILITY -> "Mobilitas bahu terbatas"
    ExerciseFlag.MOVEMENT_CORE_INSTABILITY -> "Core kurang stabil"
    ExerciseFlag.MOVEMENT_BALANCE_ASIMETRI -> "Keseimbangan tidak simetris"
}
