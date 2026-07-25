package com.fghilmany.nufitai.usecase.fullassessment

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.fake.FakeFullAssessmentRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CompleteFullAssessmentTest {

    @Test
    fun `given accumulated wizard state when invoked then persists and returns the built result`() = runTest {
        val repository = FakeFullAssessmentRepository()
        val completeFullAssessment = CompleteFullAssessment(repository)

        val result = completeFullAssessment(
            age = 29,
            gender = null,
            parQAnswers = emptyList(),
            parQGateResult = FullAssessmentParQGateResult(
                hardStopFlagged = false,
                flaggedHardStopQuestions = emptyList(),
                exerciseFlags = emptySet(),
            ),
            hardStopAcknowledgedAt = null,
            equipmentPreference = setOf(EquipmentCategory.BODYWEIGHT),
            injuryHistory = emptySet(),
            flagsPostural = emptySet(),
            movementFlags = emptySet(),
            capacityTest = null,
            goal = GoalCategory.FAT_LOSS,
            sessionsPerWeek = 3,
            selectedWeekdays = setOf(1, 3, 5),
            sessionDurationMinutes = 45,
        )

        assertIs<AppResult.Success<*>>(result)
        assertEquals(29, repository.savedResult?.age)
        assertEquals(setOf(EquipmentCategory.BODYWEIGHT), repository.savedResult?.equipmentPreference)
    }
}
