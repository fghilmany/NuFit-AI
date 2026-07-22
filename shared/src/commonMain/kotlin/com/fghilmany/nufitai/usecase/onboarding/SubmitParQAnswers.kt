package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.onboarding.entity.ParQAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.ParQResult
import com.fghilmany.nufitai.domain.onboarding.entity.toHealthFlags
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository

class SubmitParQAnswers(private val repository: OnboardingRepository) {
    suspend operator fun invoke(answers: List<ParQAnswer>): AppResult<ParQResult> {
        val flags = answers
            .filter { it.answer }
            .flatMap { it.questionId.toHealthFlags() }
            .toSet()

        val result = ParQResult(
            id = generateId(),
            answeredAt = currentInstant(),
            answers = answers,
            flagsGenerated = flags,
            requiresDoctorConsult = flags.isNotEmpty(),
            consultAcknowledgedAt = null,
        )

        val saveResult = repository.saveParQResult(result)
        return when (saveResult) {
            is AppResult.Success -> AppResult.Success(result)
            is AppResult.Error -> saveResult
        }
    }
}
