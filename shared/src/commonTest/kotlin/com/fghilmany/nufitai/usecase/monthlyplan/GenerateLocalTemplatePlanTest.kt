package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.TemplatePlanInput
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.fake.FakeExerciseLibraryRepository
import com.fghilmany.nufitai.fake.FakeMonthlyPlanRepository
import com.fghilmany.nufitai.fake.testExercise
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GenerateLocalTemplatePlanTest {

    private val bodyweightPatterns = listOf(
        MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.PUSH_HORIZONTAL,
        MovementPattern.PUSH_VERTICAL, MovementPattern.PULL_HORIZONTAL, MovementPattern.PULL_VERTICAL, MovementPattern.CORE,
    )

    private fun templateInput(resolvedSplit: ResolvedSplit, sessionsPerWeek: Int = 3) = TemplatePlanInput(
        level = Level.BEGINNER,
        goal = GoalCategory.FAT_LOSS,
        equipment = setOf(EquipmentCategory.BODYWEIGHT),
        sessionsPerWeek = sessionsPerWeek,
        resolvedSplit = resolvedSplit,
    )

    @Test
    fun `given a beginner full-body template input when invoked then produces a 30-day plan with the expected session count`() = runTest {
        val exerciseRepo = FakeExerciseLibraryRepository().apply {
            exercises = bodyweightPatterns.map { testExercise("BW-${it.name}-000", movementPattern = it) }
        }
        val planRepo = FakeMonthlyPlanRepository()
        val generate = GenerateLocalTemplatePlan(GetExercisePool(exerciseRepo), planRepo)

        val result = generate(templateInput(ResolvedSplit.FULL_BODY))

        assertIs<AppResult.Success<*>>(result)
        assertEquals(30, planRepo.savedDays.size)
        val sessionDays = planRepo.savedDays.filter { it.type == DayType.SESSION }
        assertEquals(12, sessionDays.size) // floor(3 * 30 / 7) = 12
        sessionDays.forEach { day ->
            assert(!day.mainExercises.isNullOrEmpty()) { "day ${day.dayNumber} should have main exercises" }
        }
    }

    @Test
    fun `given status DRAFT when invoked then the saved plan is not ACTIVE`() = runTest {
        val exerciseRepo = FakeExerciseLibraryRepository().apply {
            exercises = bodyweightPatterns.map { testExercise("BW-${it.name}-000", movementPattern = it) }
        }
        val planRepo = FakeMonthlyPlanRepository()
        val generate = GenerateLocalTemplatePlan(GetExercisePool(exerciseRepo), planRepo)

        val result = generate(templateInput(ResolvedSplit.FULL_BODY), status = PlanStatus.DRAFT)

        assertIs<AppResult.Success<*>>(result)
        assertEquals(PlanStatus.DRAFT, planRepo.savedPlan?.status)
    }
}
