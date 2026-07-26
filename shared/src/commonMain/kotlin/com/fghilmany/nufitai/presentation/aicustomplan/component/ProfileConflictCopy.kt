package com.fghilmany.nufitai.presentation.aicustomplan.component

import androidx.compose.runtime.Composable
import com.fghilmany.nufitai.domain.aicustomplan.entity.ProfileConflict
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.aicustomplan_conflict_sessions_per_week_beginner
import org.jetbrains.compose.resources.stringResource

/** [ProfileConflict.messageKey] is a resource-key code (architecture.md §3), never raw text -- resolved here. */
@Composable
fun ProfileConflict.copyText(): String = when (messageKey) {
    "ai_custom_plan_conflict_sessions_per_week_beginner" -> stringResource(Res.string.aicustomplan_conflict_sessions_per_week_beginner)
    else -> messageKey
}
