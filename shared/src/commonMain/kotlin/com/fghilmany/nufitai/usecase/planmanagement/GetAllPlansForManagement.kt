package com.fghilmany.nufitai.usecase.planmanagement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

/** P-14 (AC-1) -- all non-deleted plans, newest-`startedAt`-first (`getAllPlans` already filters `deletedAt`). */
class GetAllPlansForManagement(private val repository: MonthlyPlanRepository) {
    suspend operator fun invoke(): AppResult<List<MonthlyPlan>> = repository.getAllPlans()
}
