package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

data class PlanOverview(val plan: MonthlyPlan, val days: List<PlanDay>, val logs: List<PlanDaySessionLog>)

/** Bundles what P-03/P-04 both need in one read -- active plan, its days, and session logs. */
class GetActivePlanOverview(private val repository: MonthlyPlanRepository) {
    suspend operator fun invoke(): AppResult<PlanOverview?> {
        val plan = when (val result = repository.getActivePlan()) {
            is AppResult.Success -> result.data ?: return AppResult.Success(null)
            is AppResult.Error -> return result
        }
        val days = when (val result = repository.getPlanDays(plan.id)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        val logs = when (val result = repository.getSessionLogsForPlan(plan.id)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        return AppResult.Success(PlanOverview(plan, days, logs))
    }
}
