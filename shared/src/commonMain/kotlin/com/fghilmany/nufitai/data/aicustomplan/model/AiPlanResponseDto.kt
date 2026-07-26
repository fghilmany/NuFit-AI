package com.fghilmany.nufitai.data.aicustomplan.model

import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.aicustomplan.entity.AiGeneratedPlanDraft
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlannedExercise
import kotlinx.serialization.Serializable

@Serializable
data class AiPlanResponseDto(
    val smartGoalSummary: String,
    val days: List<AiPlanDayDto>,
    val conflicts: List<AiPlanConflictDto> = emptyList(),
) {
    /**
     * [PlanDay.id] is freshly generated here and [PlanDay.planId] is left blank -- both are
     * re-stamped by the caller once a real `MonthlyPlan.id` exists (`SaveAiCustomPlanDraft`/
     * `ActivateAiCustomPlan`); `MonthlyPlanLocalDataSource.insertPlan` keys `monthly_plan_id`
     * off the plan argument, not `PlanDay.planId`, so this placeholder is never persisted as-is.
     */
    fun toEntity(): AiGeneratedPlanDraft = AiGeneratedPlanDraft(
        smartGoalSummary = smartGoalSummary,
        days = days.map { it.toEntity() },
        serverNotes = conflicts.map { it.note },
    )
}

@Serializable
data class AiPlanDayDto(
    val dayNumber: Int,
    val type: String,
    val templateLetter: String? = null,
    val mainExercises: List<AiPlanExerciseDto> = emptyList(),
) {
    fun toEntity(): PlanDay = PlanDay(
        id = generateId(),
        planId = "",
        dayNumber = dayNumber,
        type = DayType.valueOf(type),
        templateLetter = templateLetter,
        warmup = null,
        mainExercises = mainExercises.map { it.toEntity() }.ifEmpty { null },
        cardio = null,
        cooldown = null,
        homework = null,
    )
}

@Serializable
data class AiPlanExerciseDto(
    val exerciseId: String,
    val sets: Int,
    val repRangeOrDuration: String,
    val rpeTargetMin: Int,
    val rpeTargetMax: Int,
    val restSeconds: Int? = null,
) {
    fun toEntity(): PlannedExercise = PlannedExercise(
        exerciseId = exerciseId,
        sets = sets,
        repRangeOrDuration = repRangeOrDuration,
        rpeTargetMin = rpeTargetMin,
        rpeTargetMax = rpeTargetMax,
        reasonRuleIds = listOf("AI_GENERATED"),
        restSeconds = restSeconds,
    )
}

@Serializable
data class AiPlanConflictDto(val field: String, val note: String)
