package com.fghilmany.nufitai.domain.fullassessment.entity

import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag

/**
 * Single source of truth for Full Assessment's GATE-01/GATE-02 semantics (issue #29 layer 1).
 * [isHardStop] questions block progression entirely (GATE-01, never become an exercise-filtering
 * flag -- mirrors issue #29's note that `flag_kehamilan` never reaches program-level filtering).
 * [toExerciseFlagOrNull] questions are GATE-02 conservative-continue conditions that map onto
 * [ExerciseFlag]'s HEALTH_* subset, feeding SAFE-06/07/08/11/12 downstream.
 */
fun FullAssessmentParQQuestionId.isHardStop(): Boolean = when (this) {
    FullAssessmentParQQuestionId.Q1_NYERI_DADA,
    FullAssessmentParQQuestionId.Q2_PINGSAN_SAAT_AKTIVITAS,
    FullAssessmentParQQuestionId.Q3_SESAK_NAPAS_AKTIVITAS_RINGAN,
    FullAssessmentParQQuestionId.Q4_OPERASI_BESAR_BELUM_PULIH,
    FullAssessmentParQQuestionId.Q5_PENGOBATAN_KANKER_AKTIF,
    FullAssessmentParQQuestionId.Q6_KEHAMILAN,
    -> true
    FullAssessmentParQQuestionId.Q7_DIABETES_TERKONTROL,
    FullAssessmentParQQuestionId.Q8_HIPERTENSI_TERKONTROL,
    FullAssessmentParQQuestionId.Q9_ASMA_TERKONTROL,
    FullAssessmentParQQuestionId.Q10_CEDERA_LAMA_PULIH,
    FullAssessmentParQQuestionId.Q11_OSTEOPOROSIS_RINGAN,
    -> false
}

fun FullAssessmentParQQuestionId.toExerciseFlagOrNull(): ExerciseFlag? = when (this) {
    FullAssessmentParQQuestionId.Q7_DIABETES_TERKONTROL -> ExerciseFlag.HEALTH_DIABETES
    FullAssessmentParQQuestionId.Q8_HIPERTENSI_TERKONTROL -> ExerciseFlag.HEALTH_TEKANAN_DARAH
    FullAssessmentParQQuestionId.Q9_ASMA_TERKONTROL -> ExerciseFlag.HEALTH_ASMA
    FullAssessmentParQQuestionId.Q10_CEDERA_LAMA_PULIH -> ExerciseFlag.HEALTH_SENDI
    FullAssessmentParQQuestionId.Q11_OSTEOPOROSIS_RINGAN -> ExerciseFlag.HEALTH_OSTEOPOROSIS
    else -> null // Kategori A (hard-stop) questions never become an exercise-filtering flag
}
