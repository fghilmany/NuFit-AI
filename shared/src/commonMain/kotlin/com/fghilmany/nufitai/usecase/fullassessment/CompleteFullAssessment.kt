package com.fghilmany.nufitai.usecase.fullassessment

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.fullassessment.entity.CapacityTestResult
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentParQAnswer
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.fullassessment.repository.FullAssessmentRepository
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlin.time.Instant

/**
 * Finalizes and persists the wizard's accumulated state as one [FullAssessmentResult].
 * AC-6 (Local -> Logged-In tier transition) is triggered by the caller after a successful
 * save -- `usecase/monthlyplan/ActivatePlanFromFullAssessment.kt` (not built yet, Stage 3).
 */
class CompleteFullAssessment(private val repository: FullAssessmentRepository) {
    suspend operator fun invoke(
        age: Int?,
        gender: Gender?,
        parQAnswers: List<FullAssessmentParQAnswer>,
        parQGateResult: FullAssessmentParQGateResult,
        hardStopAcknowledgedAt: Instant?,
        equipmentPreference: Set<EquipmentCategory>,
        injuryHistory: Set<BodyArea>,
        flagsPostural: Set<ExerciseFlag>,
        movementFlags: Set<ExerciseFlag>,
        capacityTest: CapacityTestResult?,
        goal: GoalCategory,
        sessionsPerWeek: Int,
        selectedWeekdays: Set<Int>,
        sessionDurationMinutes: Int,
    ): AppResult<FullAssessmentResult> {
        val result = FullAssessmentResult(
            id = generateId(),
            completedAt = currentInstant(),
            age = age,
            gender = gender,
            parQAnswers = parQAnswers,
            parQCategoryB = parQGateResult.exerciseFlags,
            hardStopFlagged = parQGateResult.hardStopFlagged,
            hardStopAcknowledgedAt = hardStopAcknowledgedAt,
            equipmentPreference = equipmentPreference,
            injuryHistory = injuryHistory,
            flagsPostural = flagsPostural,
            movementFlags = movementFlags,
            capacityTest = capacityTest,
            goal = goal,
            sessionsPerWeek = sessionsPerWeek,
            selectedWeekdays = selectedWeekdays,
            sessionDurationMinutes = sessionDurationMinutes,
        )
        return when (val saved = repository.saveFullAssessmentResult(result)) {
            is AppResult.Success -> AppResult.Success(result)
            is AppResult.Error -> saved
        }
    }
}
