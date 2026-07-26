package com.fghilmany.nufitai.domain.monthlyplan.entity

import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit

/**
 * issue #82 -- the 4 fields `GenerateLocalTemplatePlan` actually needs, smaller than
 * `QuickAssessmentResult` so the manual template picker (P-14 UC-4, no assessment involved)
 * can build one directly instead of faking a whole assessment result.
 */
data class TemplatePlanInput(
    val level: Level,
    val goal: GoalCategory,
    val equipment: Set<EquipmentCategory>,
    val sessionsPerWeek: Int,
    val resolvedSplit: ResolvedSplit,
)
