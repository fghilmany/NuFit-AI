package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository

class SubmitQuickAssessment(
    private val repository: OnboardingRepository,
    private val resolveQuickAssessment: ResolveQuickAssessment,
) {
    suspend operator fun invoke(input: QuickAssessmentAnswer): AppResult<QuickAssessmentResult> {
        val resolved = resolveQuickAssessment(input)

        val result = QuickAssessmentResult(
            id = generateId(),
            answeredAt = currentInstant(),
            input = input,
            level = resolved.level,
            resolvedSplit = resolved.resolvedSplit,
            splitEducationalNote = resolved.splitEducationalNote,
            templateId = resolved.templateId,
        )

        val saveResult = repository.saveQuickAssessmentResult(result)
        return when (saveResult) {
            is AppResult.Success -> AppResult.Success(result)
            is AppResult.Error -> saveResult
        }
    }
}
