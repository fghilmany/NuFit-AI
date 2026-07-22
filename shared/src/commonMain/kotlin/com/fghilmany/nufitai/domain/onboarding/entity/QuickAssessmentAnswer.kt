package com.fghilmany.nufitai.domain.onboarding.entity

data class QuickAssessmentAnswer(
    val experience: Experience,
    val goal: GoalCategory,
    val equipment: Set<EquipmentType>,
    val frequency: FrequencyBucket,
    val splitPreference: SplitPreference,
)
