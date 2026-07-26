package com.fghilmany.nufitai.usecase.planmanagement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.fake.FakeMonthlyPlanRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private fun plan(id: String, status: PlanStatus, source: PlanSource = PlanSource.LOCAL_TEMPLATE) = MonthlyPlan(
    id = id,
    startedAt = currentInstant(),
    cycleNumber = 1,
    source = source,
    status = status,
    levelMeta = "BEGINNER",
    goalMeta = GoalCategory.MUSCLE_GAIN,
    smartGoalMeta = null,
    activeFlags = emptySet(),
    startingLevelPerPattern = emptyMap(),
    mode = ProgressionMode.NORMAL,
    checkpointDays = emptyList(),
)

class ActivatePlanTest {

    @Test
    fun `given a DRAFT plan and an existing active plan when invoked then old plan archived and target activated`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            activePlan = plan("old-active", PlanStatus.ACTIVE)
            allPlans.add(activePlan!!)
            allPlans.add(plan("draft-1", PlanStatus.DRAFT))
        }
        val activatePlan = ActivatePlan(repository)

        val result = activatePlan("draft-1")

        assertIs<AppResult.Success<Unit>>(result)
        assertEquals(PlanStatus.ARCHIVED, repository.allPlans.single { it.id == "old-active" }.status)
        assertEquals(PlanStatus.ACTIVE, repository.allPlans.single { it.id == "draft-1" }.status)
    }

    @Test
    fun `given an ARCHIVED plan (Pulihkan) when invoked then it is reactivated`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            activePlan = plan("current-active", PlanStatus.ACTIVE)
            allPlans.add(activePlan!!)
            allPlans.add(plan("archived-1", PlanStatus.ARCHIVED))
        }
        val activatePlan = ActivatePlan(repository)

        val result = activatePlan("archived-1")

        assertIs<AppResult.Success<Unit>>(result)
        assertEquals(PlanStatus.ARCHIVED, repository.allPlans.single { it.id == "current-active" }.status)
        assertEquals(PlanStatus.ACTIVE, repository.allPlans.single { it.id == "archived-1" }.status)
    }

    @Test
    fun `given no existing active plan when invoked then target is activated directly`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            allPlans.add(plan("draft-1", PlanStatus.DRAFT))
        }
        val activatePlan = ActivatePlan(repository)

        val result = activatePlan("draft-1")

        assertIs<AppResult.Success<Unit>>(result)
        assertEquals(PlanStatus.ACTIVE, repository.allPlans.single { it.id == "draft-1" }.status)
    }
}
