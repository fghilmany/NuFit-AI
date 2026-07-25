package com.fghilmany.nufitai.domain.monthlyplan.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog

interface MonthlyPlanRepository {
    /** Writes the plan + all its days in one transaction (E2: no partial plan ever visible). */
    suspend fun savePlan(plan: MonthlyPlan, days: List<PlanDay>): AppResult<Unit>

    suspend fun getActivePlan(): AppResult<MonthlyPlan?>

    /** P-09 Riwayat tab: active + archived plans, newest first. */
    suspend fun getAllPlans(): AppResult<List<MonthlyPlan>>
    suspend fun getPlanDays(planId: String): AppResult<List<PlanDay>>
    suspend fun archivePlan(planId: String): AppResult<Unit>

    /** AC-10: replaces days with `dayNumber >= fromDayNumber` only -- prior days/history untouched. */
    suspend fun replacePlanDaysFrom(planId: String, fromDayNumber: Int, days: List<PlanDay>): AppResult<Unit>

    suspend fun saveSessionLog(log: PlanDaySessionLog): AppResult<Unit>
    suspend fun getSessionLogsForPlan(planId: String): AppResult<List<PlanDaySessionLog>>
}
