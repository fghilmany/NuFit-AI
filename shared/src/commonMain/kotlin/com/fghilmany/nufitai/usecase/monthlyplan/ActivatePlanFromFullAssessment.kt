package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult

/**
 * AC-6: Full Assessment completion (not login alone) archives the active Local plan and
 * activates a new Logged-In plan -- mirrors `03-assessment-detail.md` UC-3's retake-archive
 * pattern. Log history is untouched by archiving (only `MonthlyPlan.status` changes).
 */
class ActivatePlanFromFullAssessment(
    private val monthlyPlanRepository: MonthlyPlanRepository,
    private val getLatestQuickAssessmentResult: GetLatestQuickAssessmentResult,
    private val generateLoggedInPlan: GenerateLoggedInPlan,
) {
    suspend operator fun invoke(fullAssessment: FullAssessmentResult): AppResult<MonthlyPlan> {
        val activePlan = when (val result = monthlyPlanRepository.getActivePlan()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        if (activePlan != null) {
            when (val archived = monthlyPlanRepository.archivePlan(activePlan.id)) {
                is AppResult.Error -> return archived
                is AppResult.Success -> Unit
            }
        }

        val level = when (val result = getLatestQuickAssessmentResult()) {
            is AppResult.Success -> result.data?.level ?: Level.BEGINNER
            is AppResult.Error -> return result
        }

        return generateLoggedInPlan(fullAssessment, level)
    }
}
