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
    fun `given jantung question answered ya then jantung flag set and consult required`() = runTest {
        val repository = FakeOnboardingRepository()
        val submit = SubmitParQAnswers(repository)

        val result = submit(allAnswers(setOf(ParQQuestionId.Q1_JANTUNG_DIAGNOSIS)))

        val data = (result as AppResult.Success).data
        assertEquals(setOf(HealthFlag.JANTUNG), data.flagsGenerated)
        assertTrue(data.requiresDoctorConsult)
    }

    @Test
    fun `given obat rutin question answered ya then both jantung and tekanan darah flags set`() = runTest {
        val repository = FakeOnboardingRepository()
        val submit = SubmitParQAnswers(repository)

        val result = submit(allAnswers(setOf(ParQQuestionId.Q8_OBAT_RUTIN)))

        val data = (result as AppResult.Success).data
        assertEquals(setOf(HealthFlag.JANTUNG, HealthFlag.TEKANAN_DARAH), data.flagsGenerated)
    }

    @Test
    fun `given kehamilan answered ya then non-blocking -- still succeeds with kehamilan flag`() = runTest {
        val repository = FakeOnboardingRepository()
        val submit = SubmitParQAnswers(repository)

        val result = submit(allAnswers(setOf(ParQQuestionId.Q11_KEHAMILAN)))

        val data = (result as AppResult.Success).data
        assertEquals(setOf(HealthFlag.KEHAMILAN), data.flagsGenerated)
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
