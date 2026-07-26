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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

private fun aiPlan(id: String, status: PlanStatus = PlanStatus.DRAFT) = MonthlyPlan(
    id = id,
    startedAt = currentInstant(),
    cycleNumber = 1,
    source = PlanSource.AI_GENERATED,
    status = status,
    levelMeta = "BEGINNER",
    goalMeta = GoalCategory.MUSCLE_GAIN,
    smartGoalMeta = null,
    activeFlags = emptySet(),
    startingLevelPerPattern = emptyMap(),
    mode = ProgressionMode.NORMAL,
    checkpointDays = listOf(14, 21),
)

class CheckDraftQuotaTest {

    @Test
    fun `given fewer than 5 AI plans when invoked then quota is not full`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            repeat(4) { allPlans.add(aiPlan("plan-$it")) }
        }
        val checkDraftQuota = CheckDraftQuota(repository)

        val result = checkDraftQuota()

        assertIs<AppResult.Success<DraftQuotaStatus>>(result)
        assertFalse(result.data.isFull)
        assertEquals(4, result.data.existingAiPlans.size)
    }

    @Test
    fun `given exactly 5 AI plans when invoked then quota is full`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            repeat(5) { allPlans.add(aiPlan("plan-$it")) }
        }
        val checkDraftQuota = CheckDraftQuota(repository)

        val result = checkDraftQuota()

        assertIs<AppResult.Success<DraftQuotaStatus>>(result)
        assertTrue(result.data.isFull)
    }

    @Test
    fun `given archived AI plans when invoked then they do not count toward quota`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            repeat(5) { allPlans.add(aiPlan("plan-$it", status = PlanStatus.ARCHIVED)) }
        }
        val checkDraftQuota = CheckDraftQuota(repository)

        val result = checkDraftQuota()

        assertIs<AppResult.Success<DraftQuotaStatus>>(result)
        assertFalse(result.data.isFull)
        assertTrue(result.data.existingAiPlans.isEmpty())
    }

    @Test
    fun `given non AI plans when invoked then they do not count toward quota`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            repeat(5) { allPlans.add(aiPlan("plan-$it").copy(source = PlanSource.LOCAL_TEMPLATE)) }
        }
        val checkDraftQuota = CheckDraftQuota(repository)

        val result = checkDraftQuota()

        assertIs<AppResult.Success<DraftQuotaStatus>>(result)
        assertFalse(result.data.isFull)
        assertTrue(result.data.existingAiPlans.isEmpty())
    }
}
