package com.fghilmany.nufitai.usecase.assessmentdetail

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog
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
import com.fghilmany.nufitai.usecase.fullassessment.GetLatestFullAssessmentResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestBodyMeasurement
import com.fghilmany.nufitai.usecase.onboarding.GetLatestParQResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

class GetAssessmentDetailTest {

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

    private val activePlan = MonthlyPlan(
        id = "p1",
        startedAt = Instant.fromEpochMilliseconds(0),
        cycleNumber = 1,
        source = PlanSource.LOCAL_TEMPLATE,
        status = PlanStatus.ACTIVE,
        levelMeta = "BEGINNER",
        goalMeta = GoalCategory.FAT_LOSS,
        smartGoalMeta = null,
        activeFlags = emptySet(),
        startingLevelPerPattern = emptyMap(),
        mode = ProgressionMode.NORMAL,
        checkpointDays = emptyList(),
    )

    private fun buildUseCase(
        onboardingRepository: FakeOnboardingRepository = FakeOnboardingRepository(),
        fullAssessmentRepository: FakeFullAssessmentRepository = FakeFullAssessmentRepository(),
        monthlyPlanRepository: FakeMonthlyPlanRepository = FakeMonthlyPlanRepository(),
    ) = GetAssessmentDetail(
        getLatestQuickAssessmentResult = GetLatestQuickAssessmentResult(onboardingRepository),
        getLatestParQResult = GetLatestParQResult(onboardingRepository),
        getLatestFullAssessmentResult = GetLatestFullAssessmentResult(fullAssessmentRepository),
        getLatestBodyMeasurement = GetLatestBodyMeasurement(onboardingRepository),
        monthlyPlanRepository = monthlyPlanRepository,
    )

    @Test
    fun `given only quick assessment completed when invoked then full assessment is null not an error`() = runTest {
        val onboardingRepository = FakeOnboardingRepository().apply { latestQuickAssessmentResult = quickAssessment }
        val monthlyPlanRepository = FakeMonthlyPlanRepository().apply { activePlan = this@GetAssessmentDetailTest.activePlan }
        val getAssessmentDetail = buildUseCase(onboardingRepository = onboardingRepository, monthlyPlanRepository = monthlyPlanRepository)

        val result = getAssessmentDetail()

        assertIs<AppResult.Success<AssessmentDetailSummary>>(result)
        assertEquals(quickAssessment, result.data.quickAssessment)
        assertNull(result.data.fullAssessment)
        assertEquals(false, result.data.hasInProgressSession)
    }

    @Test
    fun `given no quick assessment result when invoked then returns an error`() = runTest {
        val monthlyPlanRepository = FakeMonthlyPlanRepository().apply { activePlan = this@GetAssessmentDetailTest.activePlan }
        val getAssessmentDetail = buildUseCase(monthlyPlanRepository = monthlyPlanRepository)

        val result = getAssessmentDetail()

        assertIs<AppResult.Error>(result)
    }

    @Test
    fun `given only full assessment completed when invoked then quick assessment is null not an error`() = runTest {
        val fullAssessment = FullAssessmentResult(
            id = "fa1",
            completedAt = Instant.fromEpochMilliseconds(0),
            age = 30,
            gender = null,
            parQAnswers = emptyList(),
            parQCategoryB = emptySet(),
            hardStopFlagged = false,
            hardStopAcknowledgedAt = null,
            equipmentPreference = emptySet(),
            injuryHistory = emptySet(),
            injuryHistoryNote = null,
            flagsPostural = emptySet(),
            movementFlags = emptySet(),
            capacityTest = null,
            goals = setOf(GoalCategory.FAT_LOSS),
            sessionsPerWeek = 3,
            selectedWeekdays = setOf(1, 3, 5),
            sessionDurationMinutes = 45,
        )
        val fullAssessmentRepository = FakeFullAssessmentRepository().apply { saveFullAssessmentResult(fullAssessment) }
        val monthlyPlanRepository = FakeMonthlyPlanRepository().apply {
            activePlan = this@GetAssessmentDetailTest.activePlan.copy(source = PlanSource.LOGGED_IN_RULE_ENGINE)
        }
        val getAssessmentDetail = buildUseCase(fullAssessmentRepository = fullAssessmentRepository, monthlyPlanRepository = monthlyPlanRepository)

        val result = getAssessmentDetail()

        assertIs<AppResult.Success<AssessmentDetailSummary>>(result)
        assertNull(result.data.quickAssessment)
        assertEquals(fullAssessment, result.data.fullAssessment)
    }

    @Test
    fun `given no active plan when invoked then returns an error`() = runTest {
        val onboardingRepository = FakeOnboardingRepository().apply { latestQuickAssessmentResult = quickAssessment }
        val getAssessmentDetail = buildUseCase(onboardingRepository = onboardingRepository)

        val result = getAssessmentDetail()

        assertIs<AppResult.Error>(result)
    }

    @Test
    fun `given a session log with neither completedAt nor skippedAt when invoked then flags an in-progress session`() = runTest {
        val onboardingRepository = FakeOnboardingRepository().apply { latestQuickAssessmentResult = quickAssessment }
        val monthlyPlanRepository = FakeMonthlyPlanRepository().apply {
            activePlan = this@GetAssessmentDetailTest.activePlan
            sessionLogs["p1"] = listOf(
                PlanDaySessionLog(id = "log1", planDayId = "d1", completedAt = null, skippedAt = null, rpeReported = null, painReported = null),
            )
        }
        val getAssessmentDetail = buildUseCase(onboardingRepository = onboardingRepository, monthlyPlanRepository = monthlyPlanRepository)

        val result = getAssessmentDetail()

        assertIs<AppResult.Success<AssessmentDetailSummary>>(result)
        assertEquals(true, result.data.hasInProgressSession)
    }
}
