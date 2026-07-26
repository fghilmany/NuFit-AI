package com.fghilmany.nufitai.usecase.aicustomplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

const val MAX_AI_CUSTOM_PLAN_QUOTA = 5

data class DraftQuotaStatus(val existingAiPlans: List<MonthlyPlan>, val isFull: Boolean)

/** AC-10 -- counts DRAFT + ACTIVE AI-generated plans; ARCHIVED ones don't count (techspec §5). */
class CheckDraftQuota(private val repository: MonthlyPlanRepository) {
    suspend operator fun invoke(): AppResult<DraftQuotaStatus> {
        val aiPlans = when (val result = repository.getAllPlans()) {
            is AppResult.Success -> result.data.filter { it.source == PlanSource.AI_GENERATED && it.status != PlanStatus.ARCHIVED }
            is AppResult.Error -> return result
        }
        return AppResult.Success(DraftQuotaStatus(aiPlans, aiPlans.size >= MAX_AI_CUSTOM_PLAN_QUOTA))
    }
}
