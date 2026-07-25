package com.fghilmany.nufitai.domain.onboarding.entity

/**
 * PAR-Q question -> HealthFlag mapping (source: issue #26, techspec section 5).
 * Single source of truth -- used both when a result is first submitted and when
 * an existing result is re-read from local storage.
 */
fun ParQQuestionId.toHealthFlags(): Set<HealthFlag> = when (this) {
    ParQQuestionId.Q1_HEART_DIAGNOSIS,
    ParQQuestionId.Q2_CHEST_PAIN_ACTIVITY,
    ParQQuestionId.Q3_CHEST_PAIN_REST,
    ParQQuestionId.Q4_DIZZINESS_FAINTING,
    -> setOf(HealthFlag.HEART)
    ParQQuestionId.Q5_BLOOD_PRESSURE -> setOf(HealthFlag.BLOOD_PRESSURE)
    ParQQuestionId.Q6_JOINT_PROBLEM -> setOf(HealthFlag.JOINT)
    ParQQuestionId.Q7_RECENT_SURGERY -> setOf(HealthFlag.JOINT_SURGERY_RECENT)
    ParQQuestionId.Q8_ROUTINE_MEDICATION -> setOf(HealthFlag.HEART, HealthFlag.BLOOD_PRESSURE)
    ParQQuestionId.Q9_DIABETES -> setOf(HealthFlag.DIABETES)
    ParQQuestionId.Q10_ACTIVE_CANCER -> setOf(HealthFlag.ACTIVE_CANCER)
    ParQQuestionId.Q11_PREGNANCY -> setOf(HealthFlag.PREGNANCY)
    ParQQuestionId.Q12_OTHER_CONDITION -> setOf(HealthFlag.OTHER)
}
