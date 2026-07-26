package com.fghilmany.nufitai.domain.aicustomplan.entity

import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory

/** P-12 B form input (issue #12 UC-3). */
data class AiPlanFormInput(
    val goal: GoalCategory,
    val sessionsPerWeek: Int,
    val sessionDurationMinutes: Int,
    val equipmentPreference: Set<EquipmentCategory>,
    val muscleFocus: Set<MuscleGroup>,
    val planDurationWeeks: Int,
)
