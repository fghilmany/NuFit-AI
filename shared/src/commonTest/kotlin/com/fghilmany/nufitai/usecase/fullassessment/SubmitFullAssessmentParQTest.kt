package com.fghilmany.nufitai.usecase.fullassessment

import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentParQAnswer
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentParQQuestionId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SubmitFullAssessmentParQTest {

    private val submitFullAssessmentParQ = SubmitFullAssessmentParQ()

    private fun allNo(vararg yes: FullAssessmentParQQuestionId) =
        FullAssessmentParQQuestionId.entries.map { FullAssessmentParQAnswer(it, it in yes) }

    @Test
    fun `given all answers no when invoked then no hard stop and no flags`() {
        val result = submitFullAssessmentParQ(allNo())

        assertFalse(result.hardStopFlagged)
        assertTrue(result.flaggedHardStopQuestions.isEmpty())
        assertTrue(result.exerciseFlags.isEmpty())
    }

    @Test
    fun `given a Kategori A answer yes when invoked then hard stop flagged`() {
        val result = submitFullAssessmentParQ(allNo(FullAssessmentParQQuestionId.Q6_KEHAMILAN))

        assertTrue(result.hardStopFlagged)
        assertEquals(listOf(FullAssessmentParQQuestionId.Q6_KEHAMILAN), result.flaggedHardStopQuestions)
        assertTrue(result.exerciseFlags.isEmpty()) // Kategori A never becomes an exercise-filtering flag
    }

    @Test
    fun `given a Kategori B answer yes when invoked then conservative flag without hard stop`() {
        val result = submitFullAssessmentParQ(allNo(FullAssessmentParQQuestionId.Q9_ASMA_TERKONTROL))

        assertFalse(result.hardStopFlagged)
        assertEquals(setOf(ExerciseFlag.HEALTH_ASMA), result.exerciseFlags)
    }
}
