package com.fghilmany.nufitai.usecase.planmanagement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.fake.FakeExerciseLibraryRepository
import com.fghilmany.nufitai.fake.FakeMonthlyPlanRepository
import com.fghilmany.nufitai.fake.testExercise
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import com.fghilmany.nufitai.usecase.monthlyplan.GenerateLocalTemplatePlan
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private val bodyweightPatterns = listOf(
    MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.PUSH_HORIZONTAL,
    MovementPattern.PUSH_VERTICAL, MovementPattern.PULL_HORIZONTAL, MovementPattern.PULL_VERTICAL, MovementPattern.CORE,
)

private fun existingActivePlan(id: String) = MonthlyPlan(
    id = id,
    startedAt = currentInstant(),
    cycleNumber = 1,
    source = PlanSource.LOCAL_TEMPLATE,
    status = PlanStatus.ACTIVE,
    levelMeta = "BEGINNER",
    goalMeta = GoalCategory.MUSCLE_GAIN,
    smartGoalMeta = null,
    activeFlags = emptySet(),
    startingLevelPerPattern = emptyMap(),
    mode = ProgressionMode.NORMAL,
    checkpointDays = emptyList(),
)

class CreateTemplatePlanTest {

    private fun buildUsecase(planRepo: FakeMonthlyPlanRepository): CreateTemplatePlan {
        val exerciseRepo = FakeExerciseLibraryRepository().apply {
            exercises = bodyweightPatterns.map { testExercise("BW-${it.name}-000", movementPattern = it) }
        }
        val generate = GenerateLocalTemplatePlan(GetExercisePool(exerciseRepo), planRepo)
        return CreateTemplatePlan(planRepo, generate)
    }

    @Test
    fun `given save as draft when invoked then the active plan is not touched`() = runTest {
        val planRepo = FakeMonthlyPlanRepository().apply {
            activePlan = existingActivePlan("existing-active")
            allPlans.add(activePlan!!)
        }
        val createTemplatePlan = buildUsecase(planRepo)

        val result = createTemplatePlan(Level.BEGINNER, GoalCategory.FAT_LOSS, ResolvedSplit.FULL_BODY, activateNow = false)

        assertIs<AppResult.Success<MonthlyPlan>>(result)
        assertEquals(PlanStatus.DRAFT, result.data.status)
        assertEquals(PlanStatus.ACTIVE, planRepo.allPlans.single { it.id == "existing-active" }.status)
    }

    @Test
    fun `given activate now when invoked then the prior active plan is archived`() = runTest {
        val planRepo = FakeMonthlyPlanRepository().apply {
            activePlan = existingActivePlan("existing-active")
            allPlans.add(activePlan!!)
        }
        val createTemplatePlan = buildUsecase(planRepo)

        val result = createTemplatePlan(Level.BEGINNER, GoalCategory.FAT_LOSS, ResolvedSplit.FULL_BODY, activateNow = true)

        assertIs<AppResult.Success<MonthlyPlan>>(result)
        assertEquals(PlanStatus.ACTIVE, result.data.status)
        assertEquals(PlanStatus.ARCHIVED, planRepo.allPlans.single { it.id == "existing-active" }.status)
    }

    @Test
    fun `given full body split when invoked then frequency defaults to 3 sessions per week`() = runTest {
        val planRepo = FakeMonthlyPlanRepository()
        val createTemplatePlan = buildUsecase(planRepo)

        createTemplatePlan(Level.BEGINNER, GoalCategory.FAT_LOSS, ResolvedSplit.FULL_BODY, activateNow = false)

        val sessionDays = planRepo.savedDays.count { it.type == com.fghilmany.nufitai.domain.monthlyplan.entity.DayType.SESSION }
        assertEquals(12, sessionDays) // floor(3 * 30 / 7)
    }

    @Test
    fun `given upper lower split when invoked then frequency defaults to 4 sessions per week`() = runTest {
        val planRepo = FakeMonthlyPlanRepository()
        val createTemplatePlan = buildUsecase(planRepo)

        createTemplatePlan(Level.BEGINNER, GoalCategory.FAT_LOSS, ResolvedSplit.UPPER_LOWER, activateNow = false)

        val sessionDays = planRepo.savedDays.count { it.type == com.fghilmany.nufitai.domain.monthlyplan.entity.DayType.SESSION }
        assertEquals(17, sessionDays) // floor(4 * 30 / 7)
    }
}
