package com.fghilmany.nufitai.domain.exerciselibrary.entity

/**
 * Single source of truth mapping the `Gym Techniques` source data's raw `flag_*` tokens
 * (e.g. "flag_hinge_from_back") to [ExerciseFlag]. Used by the seed data source when
 * parsing `exercises.json`, mirrors onboarding's `ParQQuestionId.toHealthFlags()` pattern.
 */
fun String.toExerciseFlagOrNull(): ExerciseFlag? = when (this) {
    "flag_jantung" -> ExerciseFlag.HEALTH_JANTUNG
    "flag_tekanan_darah" -> ExerciseFlag.HEALTH_TEKANAN_DARAH
    "flag_diabetes" -> ExerciseFlag.HEALTH_DIABETES
    "flag_asma" -> ExerciseFlag.HEALTH_ASMA
    "flag_sendi" -> ExerciseFlag.HEALTH_SENDI
    "flag_osteoporosis" -> ExerciseFlag.HEALTH_OSTEOPOROSIS
    "flag_forward_head" -> ExerciseFlag.POSTURAL_FORWARD_HEAD
    "flag_rounded_shoulder" -> ExerciseFlag.POSTURAL_ROUNDED_SHOULDER
    "flag_kyphosis" -> ExerciseFlag.POSTURAL_KYPHOSIS
    "flag_apt" -> ExerciseFlag.POSTURAL_APT
    "flag_ppt" -> ExerciseFlag.POSTURAL_PPT
    "flag_asimetri_bahu" -> ExerciseFlag.POSTURAL_ASIMETRI_BAHU
    "flag_asimetri_pinggul" -> ExerciseFlag.POSTURAL_ASIMETRI_PINGGUL
    "flag_ankle_mobility" -> ExerciseFlag.MOVEMENT_ANKLE_MOBILITY
    "flag_knee_valgus" -> ExerciseFlag.MOVEMENT_KNEE_VALGUS
    "flag_hinge_from_back" -> ExerciseFlag.MOVEMENT_HINGE_FROM_BACK
    "flag_shoulder_mobility" -> ExerciseFlag.MOVEMENT_SHOULDER_MOBILITY
    "flag_core_instability" -> ExerciseFlag.MOVEMENT_CORE_INSTABILITY
    "flag_balance_asimetri" -> ExerciseFlag.MOVEMENT_BALANCE_ASIMETRI
    else -> null
}
