package com.fghilmany.nufitai.usecase.aicustomplan

import com.fghilmany.nufitai.domain.aicustomplan.entity.AiPlanFormInput
import com.fghilmany.nufitai.domain.aicustomplan.entity.ProfileConflict
import com.fghilmany.nufitai.domain.onboarding.entity.Level

private const val HIGH_FREQUENCY_FOR_BEGINNER = 6

/**
 * AC-9 -- client-side only, purely advisory (never blocks generation). Server may also return
 * its own conflicts (`AiGeneratedPlanDraft.serverConflicts`) -- both are shown, independent of
 * each other, since the UI must never block on a slow/absent server-side check.
 */
class DetectProfileConflicts {
    operator fun invoke(formInput: AiPlanFormInput, level: Level): List<ProfileConflict> = buildList {
        if (level == Level.BEGINNER && formInput.sessionsPerWeek >= HIGH_FREQUENCY_FOR_BEGINNER) {
            add(ProfileConflict(field = "sessionsPerWeek", messageKey = "ai_custom_plan_conflict_sessions_per_week_beginner"))
        }
    }
}
