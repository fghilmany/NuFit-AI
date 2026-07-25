package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.HealthFlag
import com.fghilmany.nufitai.domain.onboarding.entity.ParQAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.ParQQuestionId
import com.fghilmany.nufitai.fake.FakeOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SubmitParQAnswersTest {

    private fun allAnswers(yesQuestions: Set<ParQQuestionId> = emptySet()): List<ParQAnswer> =
        ParQQuestionId.entries.map { ParQAnswer(it, answer = it in yesQuestions) }

    @Test
    fun `given all tidak when submitted then no flags and consult not required`() = runTest {
        val repository = FakeOnboardingRepository()
        val submit = SubmitParQAnswers(repository)

        val result = submit(allAnswers())

        assertTrue(result is AppResult.Success)
        val data = result.data
        assertTrue(data.flagsGenerated.isEmpty())
        assertFalse(data.requiresDoctorConsult)
    }

    @Test
    fun `given heart question answered yes then heart flag set and consult required`() = runTest {
        val repository = FakeOnboardingRepository()
        val submit = SubmitParQAnswers(repository)

        val result = submit(allAnswers(setOf(ParQQuestionId.Q1_HEART_DIAGNOSIS)))

        val data = (result as AppResult.Success).data
        assertEquals(setOf(HealthFlag.HEART), data.flagsGenerated)
        assertTrue(data.requiresDoctorConsult)
    }

    @Test
    fun `given routine medication question answered yes then both heart and blood pressure flags set`() = runTest {
        val repository = FakeOnboardingRepository()
        val submit = SubmitParQAnswers(repository)

        val result = submit(allAnswers(setOf(ParQQuestionId.Q8_ROUTINE_MEDICATION)))

        val data = (result as AppResult.Success).data
        assertEquals(setOf(HealthFlag.HEART, HealthFlag.BLOOD_PRESSURE), data.flagsGenerated)
    }

    @Test
    fun `given pregnancy answered yes then non-blocking -- still succeeds with pregnancy flag`() = runTest {
        val repository = FakeOnboardingRepository()
        val submit = SubmitParQAnswers(repository)

        val result = submit(allAnswers(setOf(ParQQuestionId.Q11_PREGNANCY)))

        val data = (result as AppResult.Success).data
        assertEquals(setOf(HealthFlag.PREGNANCY), data.flagsGenerated)
        assertTrue(data.requiresDoctorConsult)
    }

    @Test
    fun `submitted result is persisted via the repository`() = runTest {
        val repository = FakeOnboardingRepository()
        val submit = SubmitParQAnswers(repository)

        submit(allAnswers())

        assertTrue(repository.savedParQResult != null)
    }
}
