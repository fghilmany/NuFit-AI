package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.ActivityLevel
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

private fun bodyMeasurement(heightCm: Double? = 175.0, weightKg: Double? = 70.0) = BodyMeasurement(
    id = "bm-1",
    recordedAt = Instant.fromEpochMilliseconds(0),
    heightCm = heightCm,
    weightKg = weightKg,
)

private fun fullAssessmentResult(
    age: Int? = 30,
    gender: Gender? = Gender.MALE,
    activityLevel: ActivityLevel? = ActivityLevel.MODERATE,
    goal: GoalCategory = GoalCategory.GENERAL_HEALTH,
) = FullAssessmentResult(
    id = "fa-1",
    completedAt = Instant.fromEpochMilliseconds(0),
    age = age,
    gender = gender,
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
    goal = goal,
    sessionsPerWeek = 3,
    selectedWeekdays = emptySet(),
    sessionDurationMinutes = 45,
    activityLevel = activityLevel,
)

class CalculateTdeeAndMacrosTest {

    @Test
    fun `given complete data for a male maintaining when invoked then TDEE matches the Mifflin-St Jeor reference value`() {
        // BMR = 10*70 + 6.25*175 - 5*30 + 5 = 700 + 1093.75 - 150 + 5 = 1648.75
        // TDEE = 1648.75 * 1.55 (MODERATE) = 2555.5625 -> maintenance goal, no adjustment
        val result = CalculateTdeeAndMacros()(bodyMeasurement(), fullAssessmentResult())
        assertIs<AppResult.Success<*>>(result)
        val target = (result as AppResult.Success).data
        assertEquals(2556, target.calories)
    }

    @Test
    fun `given fat loss goal when invoked then calories are reduced from maintenance TDEE`() {
        // Maintenance TDEE = 2555.5625 (see reference test above) * 0.85 = 2172.228125 -> rounds to 2172
        val fatLoss = (CalculateTdeeAndMacros()(bodyMeasurement(), fullAssessmentResult(goal = GoalCategory.FAT_LOSS)) as AppResult.Success).data
        assertEquals(2172, fatLoss.calories)
    }

    @Test
    fun `given missing height when invoked then a validation error is returned`() {
        val result = CalculateTdeeAndMacros()(bodyMeasurement(heightCm = null), fullAssessmentResult())
        assertIs<AppResult.Error>(result)
    }

    @Test
    fun `given missing activity level when invoked then a validation error is returned`() {
        val result = CalculateTdeeAndMacros()(bodyMeasurement(), fullAssessmentResult(activityLevel = null))
        assertIs<AppResult.Error>(result)
    }
}
