package com.fghilmany.nufitai.domain.onboarding.entity

import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory

data class QuickAssessmentAnswer(
    val experience: Experience,
    val goal: GoalCategory,
    val equipment: Set<EquipmentCategory>,
    val frequency: FrequencyBucket,
    val splitPreference: SplitPreference,
)
