package com.fghilmany.nufitai.usecase.aicustomplan

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
    checkpointDays = listOf(14, 21),
)

class ActivateAiCustomPlanTest {

    @Test
    fun `given an existing active plan when invoked then it is archived and the new plan activated`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            activePlan = plan("old-plan", PlanStatus.ACTIVE)
            allPlans.add(activePlan!!)
        }
        val activateAiCustomPlan = ActivateAiCustomPlan(repository)
        val newPlan = plan("new-ai-plan", PlanStatus.DRAFT, source = PlanSource.AI_GENERATED)

        val result = activateAiCustomPlan(newPlan, emptyList())

        assertIs<AppResult.Success<MonthlyPlan>>(result)
        assertEquals(PlanStatus.ACTIVE, result.data.status)
        assertEquals(PlanStatus.ARCHIVED, repository.allPlans.single { it.id == "old-plan" }.status)
    }

    @Test
    fun `given no existing active plan when invoked then the new plan is activated directly`() = runTest {
        val repository = FakeMonthlyPlanRepository()
        val activateAiCustomPlan = ActivateAiCustomPlan(repository)
        val newPlan = plan("new-ai-plan", PlanStatus.DRAFT, source = PlanSource.AI_GENERATED)

        val result = activateAiCustomPlan(newPlan, emptyList())

        assertIs<AppResult.Success<MonthlyPlan>>(result)
        assertEquals(PlanStatus.ACTIVE, result.data.status)
    }
}
