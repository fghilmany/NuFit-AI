package com.fghilmany.nufitai.usecase.profilesettings

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.onboarding.entity.Experience
import com.fghilmany.nufitai.domain.onboarding.entity.FrequencyBucket
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.domain.onboarding.entity.SplitPreference
import com.fghilmany.nufitai.domain.profile.entity.ProfileBasics
import com.fghilmany.nufitai.fake.FakeFullAssessmentRepository
import com.fghilmany.nufitai.fake.FakeOnboardingRepository
import com.fghilmany.nufitai.fake.FakeProfileRepository
import com.fghilmany.nufitai.usecase.fullassessment.GetLatestFullAssessmentResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult
import com.fghilmany.nufitai.usecase.profile.GetProfileBasics
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

class GetProfileSettingsSummaryTest {
    private fun quickAssessment(answeredAt: Instant) = QuickAssessmentResult(
        id = "qa1",
        answeredAt = answeredAt,
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

    private fun fullAssessment(completedAt: Instant) = FullAssessmentResult(
        id = "fa1",
        completedAt = completedAt,
        age = 30,
        gender = Gender.MALE,
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
        goal = GoalCategory.FAT_LOSS,
        sessionsPerWeek = 3,
        selectedWeekdays = setOf(1, 3, 5),
        sessionDurationMinutes = 45,
    )

    private fun buildUseCase(
        profileRepository: FakeProfileRepository = FakeProfileRepository(),
        onboardingRepository: FakeOnboardingRepository = FakeOnboardingRepository(),
        fullAssessmentRepository: FakeFullAssessmentRepository = FakeFullAssessmentRepository(),
    ) = GetProfileSettingsSummary(
        getProfileBasics = GetProfileBasics(profileRepository),
        getLatestQuickAssessmentResult = GetLatestQuickAssessmentResult(onboardingRepository),
        getLatestFullAssessmentResult = GetLatestFullAssessmentResult(fullAssessmentRepository),
    )

    @Test
    fun `given neither assessment exists when invoked then latestAssessmentAt is null`() = runTest {
        val result = buildUseCase()()

        assertIs<AppResult.Success<*>>(result)
        assertNull(result.data.latestAssessmentAt)
        assertNull(result.data.profileBasics)
    }

    @Test
    fun `given only quick assessment exists when invoked then it is the latest`() = runTest {
        val answeredAt = Instant.fromEpochMilliseconds(1_000)
        val onboardingRepository = FakeOnboardingRepository().apply { latestQuickAssessmentResult = quickAssessment(answeredAt) }

        val result = buildUseCase(onboardingRepository = onboardingRepository)()

        assertIs<AppResult.Success<*>>(result)
        assertEquals(answeredAt, result.data.latestAssessmentAt)
    }

    @Test
    fun `given full assessment is more recent than quick assessment when invoked then full assessment wins`() = runTest {
        val onboardingRepository = FakeOnboardingRepository().apply {
            latestQuickAssessmentResult = quickAssessment(Instant.fromEpochMilliseconds(1_000))
        }
        val fullAssessmentRepository = FakeFullAssessmentRepository().apply {
            latestResult = fullAssessment(Instant.fromEpochMilliseconds(5_000))
        }

        val result = buildUseCase(onboardingRepository = onboardingRepository, fullAssessmentRepository = fullAssessmentRepository)()

        assertIs<AppResult.Success<*>>(result)
        assertEquals(Instant.fromEpochMilliseconds(5_000), result.data.latestAssessmentAt)
    }

    @Test
    fun `given a completed profile when invoked then profileBasics is returned`() = runTest {
        val basics = ProfileBasics(age = 28, gender = Gender.FEMALE, updatedAt = Instant.fromEpochMilliseconds(0))
        val profileRepository = FakeProfileRepository(basics)

        val result = buildUseCase(profileRepository = profileRepository)()

        assertIs<AppResult.Success<*>>(result)
        assertEquals(basics, result.data.profileBasics)
    }
}
