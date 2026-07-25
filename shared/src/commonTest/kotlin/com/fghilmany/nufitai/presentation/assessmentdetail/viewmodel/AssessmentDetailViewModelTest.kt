package com.fghilmany.nufitai.presentation.assessmentdetail.viewmodel

import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.domain.onboarding.entity.Experience
import com.fghilmany.nufitai.domain.onboarding.entity.FrequencyBucket
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.domain.onboarding.entity.SplitPreference
import com.fghilmany.nufitai.fake.FakeFullAssessmentRepository
import com.fghilmany.nufitai.fake.FakeMonthlyPlanRepository
import com.fghilmany.nufitai.fake.FakeOnboardingRepository
import com.fghilmany.nufitai.usecase.assessmentdetail.GetAssessmentDetail
import com.fghilmany.nufitai.usecase.assessmentdetail.GetAssessmentHistory
import com.fghilmany.nufitai.usecase.assessmentdetail.RetakeAssessment
import com.fghilmany.nufitai.usecase.fullassessment.GetLatestFullAssessmentResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestBodyMeasurement
import com.fghilmany.nufitai.usecase.onboarding.GetLatestParQResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult
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
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AssessmentDetailViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val quickAssessment = QuickAssessmentResult(
        id = "qa1",
        answeredAt = Instant.fromEpochMilliseconds(0),
        input = QuickAssessmentAnswer(
            experience = Experience.NEVER,
            goal = GoalCategory.FAT_LOSS,
            equipment = emptySet(),
            frequency = FrequencyBucket.THREE,
            splitPreference = SplitPreference.FULL_BODY,
        ),
        level = Level.BEGINNER,
        resolvedSplit = ResolvedSplit.FULL_BODY,
        splitEducationalNote = null,
        templateId = "T1",
    )

    private fun activePlan(source: PlanSource = PlanSource.LOCAL_TEMPLATE) = MonthlyPlan(
        id = "p1",
        startedAt = Instant.fromEpochMilliseconds(0),
        cycleNumber = 1,
        source = source,
        status = PlanStatus.ACTIVE,
        levelMeta = "BEGINNER",
        goalMeta = GoalCategory.FAT_LOSS,
        smartGoalMeta = null,
        activeFlags = emptySet(),
        startingLevelPerPattern = emptyMap(),
        mode = ProgressionMode.NORMAL,
        checkpointDays = emptyList(),
    )

    private fun viewModel(
        onboardingRepository: FakeOnboardingRepository = FakeOnboardingRepository().apply { latestQuickAssessmentResult = quickAssessment },
        monthlyPlanRepository: FakeMonthlyPlanRepository = FakeMonthlyPlanRepository().apply { activePlan = activePlan() },
        fullAssessmentRepository: FakeFullAssessmentRepository = FakeFullAssessmentRepository(),
    ) = AssessmentDetailViewModel(
        getAssessmentDetail = GetAssessmentDetail(
            getLatestQuickAssessmentResult = GetLatestQuickAssessmentResult(onboardingRepository),
            getLatestParQResult = GetLatestParQResult(onboardingRepository),
            getLatestFullAssessmentResult = GetLatestFullAssessmentResult(fullAssessmentRepository),
            getLatestBodyMeasurement = GetLatestBodyMeasurement(onboardingRepository),
            monthlyPlanRepository = monthlyPlanRepository,
        ),
        getAssessmentHistory = GetAssessmentHistory(monthlyPlanRepository),
        retakeAssessment = RetakeAssessment(monthlyPlanRepository),
    ) to monthlyPlanRepository

    @Test
    fun `given a completed quick assessment when loaded then state is Loaded`() = runTest {
        val (vm, _) = viewModel()
        assertIs<AssessmentDetailState.Loaded>(vm.state.value)
    }

    @Test
    fun `given retake requested when confirmed then plan is archived and state becomes RetakeReady`() = runTest {
        val (vm, repository) = viewModel()

        vm.onEvent(AssessmentDetailEvent.RequestRetake)
        val confirming = vm.state.value as AssessmentDetailState.Loaded
        assertEquals(RetakeDialogState.Confirming, confirming.retakeDialog)

        vm.onEvent(AssessmentDetailEvent.ConfirmRetake)

        val finalState = vm.state.value
        assertIs<AssessmentDetailState.RetakeReady>(finalState)
        assertEquals(PlanSource.LOCAL_TEMPLATE, finalState.source)
        assertEquals(listOf("p1"), repository.archivedPlanIds)
    }

    @Test
    fun `given retake requested when cancelled then no repository writes happen`() = runTest {
        val (vm, repository) = viewModel()

        vm.onEvent(AssessmentDetailEvent.RequestRetake)
        vm.onEvent(AssessmentDetailEvent.CancelRetake)

        val state = vm.state.value as AssessmentDetailState.Loaded
        assertEquals(RetakeDialogState.Hidden, state.retakeDialog)
        assertTrue(repository.archivedPlanIds.isEmpty())
    }

    @Test
    fun `given an in-progress session when retake requested then dialog is blocked instead of confirming`() = runTest {
        val monthlyPlanRepository = FakeMonthlyPlanRepository().apply {
            activePlan = activePlan()
            sessionLogs["p1"] = listOf(
                com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog(
                    id = "log1",
                    planDayId = "d1",
                    completedAt = null,
                    skippedAt = null,
                    rpeReported = null,
                    painReported = null,
                ),
            )
        }
        val (vm, _) = viewModel(monthlyPlanRepository = monthlyPlanRepository)

        vm.onEvent(AssessmentDetailEvent.RequestRetake)

        val state = vm.state.value as AssessmentDetailState.Loaded
        assertEquals(RetakeDialogState.Blocked, state.retakeDialog)
    }
}
