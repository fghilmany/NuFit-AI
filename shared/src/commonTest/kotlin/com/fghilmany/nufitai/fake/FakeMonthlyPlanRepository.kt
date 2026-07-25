package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

class FakeMonthlyPlanRepository : MonthlyPlanRepository {
    var savedPlan: MonthlyPlan? = null
        private set
    var savedDays: List<PlanDay> = emptyList()
        private set
    var activePlan: MonthlyPlan? = null
    var planDays: MutableMap<String, List<PlanDay>> = mutableMapOf()
    var sessionLogs: MutableMap<String, List<PlanDaySessionLog>> = mutableMapOf()
    var archivedPlanIds: MutableList<String> = mutableListOf()

    /** Every plan ever saved/archived, newest-`startedAt`-first -- backs `getAllPlans()` (P-09 Riwayat, issue #77). */
    var allPlans: MutableList<MonthlyPlan> = mutableListOf()

    override suspend fun savePlan(plan: MonthlyPlan, days: List<PlanDay>): AppResult<Unit> {
        savedPlan = plan
        savedDays = days
        activePlan = plan
        planDays[plan.id] = days
        allPlans.add(plan)
        return AppResult.Success(Unit)
    }

    override suspend fun getActivePlan(): AppResult<MonthlyPlan?> = AppResult.Success(activePlan)

    override suspend fun getAllPlans(): AppResult<List<MonthlyPlan>> =
        AppResult.Success(allPlans.sortedByDescending { it.startedAt })

    override suspend fun getPlanDays(planId: String): AppResult<List<PlanDay>> = AppResult.Success(planDays[planId].orEmpty())

    override suspend fun archivePlan(planId: String): AppResult<Unit> {
        archivedPlanIds.add(planId)
        if (activePlan?.id == planId) activePlan = activePlan?.copy(status = PlanStatus.ARCHIVED)
        val index = allPlans.indexOfFirst { it.id == planId }
        if (index >= 0) allPlans[index] = allPlans[index].copy(status = PlanStatus.ARCHIVED)
        return AppResult.Success(Unit)
    }

    override suspend fun replacePlanDaysFrom(planId: String, fromDayNumber: Int, days: List<PlanDay>): AppResult<Unit> {
        val existing = planDays[planId].orEmpty().filter { it.dayNumber < fromDayNumber }
        planDays[planId] = existing + days
        return AppResult.Success(Unit)
    }

    override suspend fun saveSessionLog(log: PlanDaySessionLog): AppResult<Unit> {
        val planId = planDays.entries.find { entry -> entry.value.any { it.id == log.planDayId } }?.key
        if (planId != null) sessionLogs[planId] = sessionLogs[planId].orEmpty() + log
        return AppResult.Success(Unit)
    }

    override suspend fun getSessionLogsForPlan(planId: String): AppResult<List<PlanDaySessionLog>> =
        AppResult.Success(sessionLogs[planId].orEmpty())
}
