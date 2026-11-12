package com.fghilmany.nufitai.presentation.fullassessment.component

import androidx.compose.runtime.Composable
import com.fghilmany.nufitai.domain.fullassessment.entity.ActivityLevel
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.fullassessment_activity_active
import nufitai.shared.generated.resources.fullassessment_activity_light
import nufitai.shared.generated.resources.fullassessment_activity_moderate
import nufitai.shared.generated.resources.fullassessment_activity_sedentary
import nufitai.shared.generated.resources.fullassessment_activity_very_active
import nufitai.shared.generated.resources.fullassessment_goal_endurance_title
import nufitai.shared.generated.resources.onboarding_goal_fat_loss_title
import nufitai.shared.generated.resources.onboarding_goal_general_health_title
import nufitai.shared.generated.resources.onboarding_goal_muscle_gain_title
import nufitai.shared.generated.resources.onboarding_goal_strength_title
import org.jetbrains.compose.resources.stringResource

/** Reuses Quick Assessment's goal copy (same [GoalCategory] enum) for the 4 goals it already
 * covers; ENDURANCE is only reachable via Full Assessment's richer picker, so it gets its own string. */
@Composable
fun GoalCategory.label(): String = when (this) {
    GoalCategory.FAT_LOSS -> stringResource(Res.string.onboarding_goal_fat_loss_title)
    GoalCategory.MUSCLE_GAIN -> stringResource(Res.string.onboarding_goal_muscle_gain_title)
    GoalCategory.GENERAL_HEALTH -> stringResource(Res.string.onboarding_goal_general_health_title)
    GoalCategory.STRENGTH -> stringResource(Res.string.onboarding_goal_strength_title)
    GoalCategory.ENDURANCE -> stringResource(Res.string.fullassessment_goal_endurance_title)
}

@Composable
fun ActivityLevel.shortLabel(): String = when (this) {
    ActivityLevel.SEDENTARY -> stringResource(Res.string.fullassessment_activity_sedentary)
    ActivityLevel.LIGHT -> stringResource(Res.string.fullassessment_activity_light)
    ActivityLevel.MODERATE -> stringResource(Res.string.fullassessment_activity_moderate)
    ActivityLevel.ACTIVE -> stringResource(Res.string.fullassessment_activity_active)
    ActivityLevel.VERY_ACTIVE -> stringResource(Res.string.fullassessment_activity_very_active)
}
