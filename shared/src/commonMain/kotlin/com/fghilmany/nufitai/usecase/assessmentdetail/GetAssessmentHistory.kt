package com.fghilmany.nufitai.usecase.assessmentdetail

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

/** UC-2 (03-assessment-detail.md, issue #77): P-09 Riwayat tab, one row per MonthlyPlan cycle. */
class GetAssessmentHistory(private val monthlyPlanRepository: MonthlyPlanRepository) {
    suspend operator fun invoke(): AppResult<List<MonthlyPlan>> = monthlyPlanRepository.getAllPlans()
}
