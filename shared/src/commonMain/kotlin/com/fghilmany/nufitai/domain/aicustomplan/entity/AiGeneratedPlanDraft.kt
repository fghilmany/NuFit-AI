package com.fghilmany.nufitai.domain.aicustomplan.entity

import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay

/**
 * Raw response from the AI backend, not yet persisted or validated -- reuses [PlanDay] /
 * `PlannedExercise` shapes directly (techspec §5) so PT Mode/Home need zero changes to run an
 * AI plan once this draft becomes a [com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan].
 * [days]' `PlannedExercise.exerciseId` values are NOT yet guaranteed valid -- see
 * `usecase/aicustomplan/ValidateAndFixExerciseIds.kt`.
 */
data class AiGeneratedPlanDraft(
    val smartGoalSummary: String,
    val days: List<PlanDay>,
    /** Freeform text from the AI backend (e.g. "you asked for 6 days but as Beginner we suggest 4") --
     * content, not an app-authored string-resource key, so it's rendered as-is (like [smartGoalSummary]). */
    val serverNotes: List<String>,
)
