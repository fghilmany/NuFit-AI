package com.fghilmany.nufitai.usecase.aicustomplan

import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanFormInput
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import kotlin.test.Test
import kotlin.test.assertTrue

private fun formInput(sessionsPerWeek: Int) = AiPlanFormInput(
    goal = GoalCategory.MUSCLE_GAIN,
    sessionsPerWeek = sessionsPerWeek,
    sessionDurationMinutes = 45,
    equipmentPreference = setOf(EquipmentCategory.BODYWEIGHT),
    muscleFocus = emptySet(),
    planDurationWeeks = 2,
)

class DetectProfileConflictsTest {

    @Test
    fun `given beginner requesting 6 sessions per week when invoked then a conflict is reported`() {
        val conflicts = DetectProfileConflicts()(formInput(sessionsPerWeek = 6), Level.BEGINNER)
        assertTrue(conflicts.any { it.field == "sessionsPerWeek" })
    }

    @Test
    fun `given beginner requesting 3 sessions per week when invoked then no conflict`() {
        val conflicts = DetectProfileConflicts()(formInput(sessionsPerWeek = 3), Level.BEGINNER)
        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun `given intermediate requesting 6 sessions per week when invoked then no conflict`() {
        val conflicts = DetectProfileConflicts()(formInput(sessionsPerWeek = 6), Level.INTERMEDIATE)
        assertTrue(conflicts.isEmpty())
    }
}
