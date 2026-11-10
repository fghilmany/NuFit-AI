package com.fghilmany.nufitai.presentation.aicustomplan.viewmodel

import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanFormInput
import com.fghilmany.nufitai.domain.aicustomplan.entity.ExerciseValidationWarning
import com.fghilmany.nufitai.domain.aicustomplan.entity.InjuryConflict
import com.fghilmany.nufitai.domain.aicustomplan.entity.ProfileConflict
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay

/**
 * issue #81 -- in-memory-only bridge between P-12 B (form/generate) and P-13 (preview/edit),
 * a single Koin instance shared across both ViewModels. A generated draft is deliberately NOT
 * persisted until the user explicitly chooses "Simpan Draft" or "Aktifkan Sekarang" (UC-3/UC-4);
 * a real MonthlyPlan/PlanDay route argument would be an awkward, oversized nav payload, so this
 * avoids either premature persistence or bloated navigation routes.
 */
class AiCustomPlanDraftHolder {
    var current: PendingAiPlanDraft? = null
}

data class PendingAiPlanDraft(
    val formInput: AiPlanFormInput,
    /** User-entered plan name (P-13 preview) -- kept here, not just transient ViewModel state, so
     * it survives a "Regenerate" round-trip (`toLoadedState()`/`draft.copy()` both preserve it). */
    val planName: String = "",
    val smartGoalSummary: String,
    val days: List<PlanDay>,
    val conflicts: List<ProfileConflict>,
    val serverNotes: List<String>,
    val exerciseWarnings: List<ExerciseValidationWarning>,
    val injuryConflicts: List<InjuryConflict>,
)
