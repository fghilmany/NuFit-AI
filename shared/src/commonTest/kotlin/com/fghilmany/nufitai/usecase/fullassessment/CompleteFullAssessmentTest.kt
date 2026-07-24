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
            usia = 29,
            gender = null,
            parQAnswers = emptyList(),
            parQGateResult = FullAssessmentParQGateResult(
                hardStopFlagged = false,
                flaggedHardStopQuestions = emptyList(),
                exerciseFlags = emptySet(),
            ),
            hardStopAcknowledgedAt = null,
            preferensiAlat = setOf(EquipmentCategory.BODYWEIGHT),
            riwayatCedera = emptySet(),
            flagsPostural = emptySet(),
            flagsGerak = emptySet(),
            capacityTest = null,
            goal = GoalCategory.FAT_LOSS,
            frekuensiPerMinggu = 3,
            hariPilihan = setOf(1, 3, 5),
            durasiSesiMenit = 45,
        )

        assertIs<AppResult.Success<*>>(result)
        assertEquals(29, repository.savedResult?.usia)
        assertEquals(setOf(EquipmentCategory.BODYWEIGHT), repository.savedResult?.preferensiAlat)
    }
}
