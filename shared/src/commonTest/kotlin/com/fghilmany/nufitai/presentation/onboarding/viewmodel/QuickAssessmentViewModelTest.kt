package com.fghilmany.nufitai.presentation.onboarding.viewmodel

import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.onboarding.entity.Experience
import com.fghilmany.nufitai.domain.onboarding.entity.FrequencyBucket
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.SplitPreference
import com.fghilmany.nufitai.fake.FakeExerciseLibraryRepository
import com.fghilmany.nufitai.fake.FakeMonthlyPlanRepository
import com.fghilmany.nufitai.fake.FakeOnboardingRepository
import com.fghilmany.nufitai.fake.testExercise
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import com.fghilmany.nufitai.usecase.monthlyplan.GenerateLocalTemplatePlan
import com.fghilmany.nufitai.usecase.onboarding.ResolveQuickAssessment
import com.fghilmany.nufitai.usecase.onboarding.SubmitQuickAssessment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QuickAssessmentViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given complete five-step answers when submitted then it saves assessment and active local plan`() = runTest {
        val onboardingRepository = FakeOnboardingRepository()
        val monthlyPlanRepository = FakeMonthlyPlanRepository()
        val viewModel = viewModel(onboardingRepository, monthlyPlanRepository)

        viewModel.onEvent(QuickAssessmentEvent.SelectExperience(Experience.NEVER))
        viewModel.onEvent(QuickAssessmentEvent.NextStep)
        viewModel.onEvent(QuickAssessmentEvent.SelectGoal(GoalCategory.FAT_LOSS))
        viewModel.onEvent(QuickAssessmentEvent.NextStep)
        viewModel.onEvent(QuickAssessmentEvent.ToggleEquipment(EquipmentCategory.BODYWEIGHT))
        viewModel.onEvent(QuickAssessmentEvent.NextStep)
        viewModel.onEvent(QuickAssessmentEvent.SelectFrequency(FrequencyBucket.THREE))
        viewModel.onEvent(QuickAssessmentEvent.NextStep)
        viewModel.onEvent(QuickAssessmentEvent.SelectSplit(SplitPreference.FULL_BODY))
        viewModel.onEvent(QuickAssessmentEvent.NextStep)

        val completed = assertIs<QuickAssessmentState.Completed>(viewModel.state.value)
        assertEquals("BEGINNER_FAT_LOSS_FULL_BODY_THREE", completed.result.templateId)
        assertEquals(completed.result, onboardingRepository.savedQuickAssessmentResult)
        assertEquals(30, monthlyPlanRepository.savedDays.size)
        assertTrue(monthlyPlanRepository.savedPlan != null)
    }

    @Test
    fun `given prior answers when moving back then they remain selected`() {
        val viewModel = viewModel(FakeOnboardingRepository(), FakeMonthlyPlanRepository())

        viewModel.onEvent(QuickAssessmentEvent.SelectExperience(Experience.UNDER_1_YEAR))
        viewModel.onEvent(QuickAssessmentEvent.NextStep)
        viewModel.onEvent(QuickAssessmentEvent.SelectGoal(GoalCategory.STRENGTH))
        viewModel.onEvent(QuickAssessmentEvent.PreviousStep)

        val step = assertIs<QuickAssessmentState.Step>(viewModel.state.value)
        assertEquals(1, step.stepIndex)
        assertEquals(Experience.UNDER_1_YEAR, step.experience)
        assertEquals(GoalCategory.STRENGTH, step.goal)
    }

    private fun viewModel(
        onboardingRepository: FakeOnboardingRepository,
        monthlyPlanRepository: FakeMonthlyPlanRepository,
    ): QuickAssessmentViewModel {
        val exerciseRepository = FakeExerciseLibraryRepository().apply {
            exercises = listOf(
                MovementPattern.SQUAT,
                MovementPattern.HINGE,
                MovementPattern.PUSH_HORIZONTAL,
                MovementPattern.PUSH_VERTICAL,
                MovementPattern.PULL_HORIZONTAL,
                MovementPattern.PULL_VERTICAL,
                MovementPattern.CORE,
            ).map { pattern -> testExercise("bodyweight-${pattern.name}", movementPattern = pattern) }
        }
        return QuickAssessmentViewModel(
            submitQuickAssessment = SubmitQuickAssessment(onboardingRepository, ResolveQuickAssessment()),
            generateLocalTemplatePlan = GenerateLocalTemplatePlan(
                getExercisePool = GetExercisePool(exerciseRepository),
                repository = monthlyPlanRepository,
            ),
        )
    }
}
