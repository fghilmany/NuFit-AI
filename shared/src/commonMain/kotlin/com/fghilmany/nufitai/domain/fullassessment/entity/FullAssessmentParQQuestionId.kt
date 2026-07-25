package com.fghilmany.nufitai.domain.fullassessment.entity

/**
 * Full Assessment's PAR-Q Category A+B (issue #29 GATE-01/GATE-02), distinct from onboarding's
 * `ParQQuestionId` (Quick Assessment's simpler PAR-Q-ringkas). Re-asked here because health
 * state can change between Quick Assessment (Local tier) and Full Assessment (Logged-In tier).
 * Q1-Q6 = Category A hard-stop (GATE-01); Q7-Q11 = Category B conservative-continue (GATE-02).
 */
enum class FullAssessmentParQQuestionId {
    Q1_CHEST_PAIN,
    Q2_FAINTING_DURING_ACTIVITY,
    Q3_BREATHLESS_LIGHT_ACTIVITY,
    Q4_MAJOR_SURGERY_NOT_RECOVERED,
    Q5_ACTIVE_CANCER_TREATMENT,
    Q6_PREGNANCY,
    Q7_DIABETES_CONTROLLED,
    Q8_HYPERTENSION_CONTROLLED,
    Q9_ASTHMA_CONTROLLED,
    Q10_OLD_INJURY_RECOVERED,
    Q11_MILD_OSTEOPOROSIS,
}
