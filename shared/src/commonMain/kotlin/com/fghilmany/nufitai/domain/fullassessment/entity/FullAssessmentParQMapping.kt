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
    FullAssessmentParQQuestionId.Q1_CHEST_PAIN,
    FullAssessmentParQQuestionId.Q2_FAINTING_DURING_ACTIVITY,
    FullAssessmentParQQuestionId.Q3_BREATHLESS_LIGHT_ACTIVITY,
    FullAssessmentParQQuestionId.Q4_MAJOR_SURGERY_NOT_RECOVERED,
    FullAssessmentParQQuestionId.Q5_ACTIVE_CANCER_TREATMENT,
    FullAssessmentParQQuestionId.Q6_PREGNANCY,
    -> true
    FullAssessmentParQQuestionId.Q7_DIABETES_CONTROLLED,
    FullAssessmentParQQuestionId.Q8_HYPERTENSION_CONTROLLED,
    FullAssessmentParQQuestionId.Q9_ASTHMA_CONTROLLED,
    FullAssessmentParQQuestionId.Q10_OLD_INJURY_RECOVERED,
    FullAssessmentParQQuestionId.Q11_MILD_OSTEOPOROSIS,
    -> false
}

fun FullAssessmentParQQuestionId.toExerciseFlagOrNull(): ExerciseFlag? = when (this) {
    FullAssessmentParQQuestionId.Q7_DIABETES_CONTROLLED -> ExerciseFlag.HEALTH_DIABETES
    FullAssessmentParQQuestionId.Q8_HYPERTENSION_CONTROLLED -> ExerciseFlag.HEALTH_BLOOD_PRESSURE
    FullAssessmentParQQuestionId.Q9_ASTHMA_CONTROLLED -> ExerciseFlag.HEALTH_ASTHMA
    FullAssessmentParQQuestionId.Q10_OLD_INJURY_RECOVERED -> ExerciseFlag.HEALTH_JOINT
    FullAssessmentParQQuestionId.Q11_MILD_OSTEOPOROSIS -> ExerciseFlag.HEALTH_OSTEOPOROSIS
    else -> null // Category A (hard-stop) questions never become an exercise-filtering flag
}
