package com.fghilmany.nufitai.data.aicustomplan.model

import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanFormInput
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanGenerationRequest
import com.fghilmany.nufitai.domain.aicustomplan.entity.WorkoutHistorySummary
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import kotlinx.serialization.Serializable

@Serializable
data class AiPlanRequestDto(
    val formInput: AiPlanFormInputDto,
    val fullAssessment: FullAssessmentDto,
    val workoutHistorySummary: WorkoutHistorySummaryDto,
) {
    companion object {
        fun from(request: AiPlanGenerationRequest): AiPlanRequestDto = AiPlanRequestDto(
            formInput = AiPlanFormInputDto.from(request.formInput),
            fullAssessment = FullAssessmentDto.from(request.fullAssessment),
            workoutHistorySummary = WorkoutHistorySummaryDto.from(request.workoutHistorySummary),
        )
    }
}

@Serializable
data class AiPlanFormInputDto(
    val goal: String,
    val sessionsPerWeek: Int,
    val sessionDurationMinutes: Int,
    val equipmentPreference: Set<String>,
    val muscleFocus: Set<String>,
    val planDurationWeeks: Int,
) {
    companion object {
        fun from(input: AiPlanFormInput): AiPlanFormInputDto = AiPlanFormInputDto(
            goal = input.goal.name,
            sessionsPerWeek = input.sessionsPerWeek,
            sessionDurationMinutes = input.sessionDurationMinutes,
            equipmentPreference = input.equipmentPreference.map { it.name }.toSet(),
            muscleFocus = input.muscleFocus.map { it.name }.toSet(),
            planDurationWeeks = input.planDurationWeeks,
        )
    }
}

/** issue #81 -- request-side projection of [FullAssessmentResult]; not the full entity 1:1 (capacity test omitted, not needed by the generate contract). */
@Serializable
data class FullAssessmentDto(
    val age: Int?,
    val gender: String?,
    val parQCategoryB: Set<String>,
    val equipmentPreference: Set<String>,
    val injuryHistory: Set<String>,
    val injuryHistoryNote: String?,
    val flagsPostural: Set<String>,
    val movementFlags: Set<String>,
    val goal: String,
    val sessionsPerWeek: Int,
    val selectedWeekdays: Set<Int>,
    val sessionDurationMinutes: Int,
) {
    companion object {
        fun from(result: FullAssessmentResult): FullAssessmentDto = FullAssessmentDto(
            age = result.age,
            gender = result.gender?.name,
            parQCategoryB = result.parQCategoryB.map { it.name }.toSet(),
            equipmentPreference = result.equipmentPreference.map { it.name }.toSet(),
            injuryHistory = result.injuryHistory.map { it.name }.toSet(),
            injuryHistoryNote = result.injuryHistoryNote,
            flagsPostural = result.flagsPostural.map { it.name }.toSet(),
            movementFlags = result.movementFlags.map { it.name }.toSet(),
            goal = result.goal.name,
            sessionsPerWeek = result.sessionsPerWeek,
            selectedWeekdays = result.selectedWeekdays,
            sessionDurationMinutes = result.sessionDurationMinutes,
        )
    }
}

@Serializable
data class WorkoutHistorySummaryDto(
    val cycleCount: Int,
    val attendanceRate: Float,
    val averageRpe: Float?,
    val completedSessionCount: Int,
) {
    companion object {
        fun from(summary: WorkoutHistorySummary): WorkoutHistorySummaryDto = WorkoutHistorySummaryDto(
            cycleCount = summary.cycleCount,
            attendanceRate = summary.attendanceRate,
            averageRpe = summary.averageRpe,
            completedSessionCount = summary.completedSessionCount,
        )
    }
}
