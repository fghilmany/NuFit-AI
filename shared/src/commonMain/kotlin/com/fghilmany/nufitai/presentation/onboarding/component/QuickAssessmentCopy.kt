package com.fghilmany.nufitai.presentation.onboarding.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.fghilmany.nufitai.domain.onboarding.entity.Experience
import com.fghilmany.nufitai.domain.onboarding.entity.FrequencyBucket
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.SplitPreference
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.onboarding_experience_never_subtitle
import nufitai.shared.generated.resources.onboarding_experience_never_title
import nufitai.shared.generated.resources.onboarding_experience_under_1y_subtitle
import nufitai.shared.generated.resources.onboarding_experience_under_1y_title
import nufitai.shared.generated.resources.onboarding_experience_years_routine_subtitle
import nufitai.shared.generated.resources.onboarding_experience_years_routine_title
import nufitai.shared.generated.resources.onboarding_frequency_four_to_five_subtitle
import nufitai.shared.generated.resources.onboarding_frequency_four_to_five_title
import nufitai.shared.generated.resources.onboarding_frequency_two_to_three_subtitle
import nufitai.shared.generated.resources.onboarding_frequency_two_to_three_title
import nufitai.shared.generated.resources.onboarding_goal_fat_loss_subtitle
import nufitai.shared.generated.resources.onboarding_goal_fat_loss_title
import nufitai.shared.generated.resources.onboarding_goal_general_health_subtitle
import nufitai.shared.generated.resources.onboarding_goal_general_health_title
import nufitai.shared.generated.resources.onboarding_goal_muscle_gain_subtitle
import nufitai.shared.generated.resources.onboarding_goal_muscle_gain_title
import nufitai.shared.generated.resources.onboarding_goal_strength_subtitle
import nufitai.shared.generated.resources.onboarding_goal_strength_title
import nufitai.shared.generated.resources.onboarding_split_full_body_subtitle
import nufitai.shared.generated.resources.onboarding_split_full_body_title
import nufitai.shared.generated.resources.onboarding_split_unknown_subtitle
import nufitai.shared.generated.resources.onboarding_split_unknown_title
import nufitai.shared.generated.resources.onboarding_split_upper_lower_subtitle
import nufitai.shared.generated.resources.onboarding_split_upper_lower_title
import org.jetbrains.compose.resources.stringResource

data class OptionCopy(val icon: ImageVector, val title: String, val subtitle: String)

@Composable
fun experienceOptions(): List<Pair<Experience, OptionCopy>> = listOf(
    Experience.BELUM_PERNAH to OptionCopy(
        Icons.Filled.EmojiPeople,
        stringResource(Res.string.onboarding_experience_never_title),
        stringResource(Res.string.onboarding_experience_never_subtitle),
    ),
    Experience.KURANG_1_TAHUN to OptionCopy(
        Icons.Filled.TrendingUp,
        stringResource(Res.string.onboarding_experience_under_1y_title),
        stringResource(Res.string.onboarding_experience_under_1y_subtitle),
    ),
    Experience.RUTIN_BERTAHUN to OptionCopy(
        Icons.Filled.WorkspacePremium,
        stringResource(Res.string.onboarding_experience_years_routine_title),
        stringResource(Res.string.onboarding_experience_years_routine_subtitle),
    ),
)

@Composable
fun goalOptions(): List<Pair<GoalCategory, OptionCopy>> = listOf(
    GoalCategory.FAT_LOSS to OptionCopy(
        Icons.Filled.LocalFireDepartment,
        stringResource(Res.string.onboarding_goal_fat_loss_title),
        stringResource(Res.string.onboarding_goal_fat_loss_subtitle),
    ),
    GoalCategory.MUSCLE_GAIN to OptionCopy(
        Icons.Filled.FitnessCenter,
        stringResource(Res.string.onboarding_goal_muscle_gain_title),
        stringResource(Res.string.onboarding_goal_muscle_gain_subtitle),
    ),
    GoalCategory.GENERAL_HEALTH to OptionCopy(
        Icons.Filled.Favorite,
        stringResource(Res.string.onboarding_goal_general_health_title),
        stringResource(Res.string.onboarding_goal_general_health_subtitle),
    ),
    GoalCategory.STRENGTH to OptionCopy(
        Icons.Filled.Bolt,
        stringResource(Res.string.onboarding_goal_strength_title),
        stringResource(Res.string.onboarding_goal_strength_subtitle),
    ),
)

@Composable
fun frequencyOptions(): List<Pair<FrequencyBucket, OptionCopy>> = listOf(
    FrequencyBucket.TWO_TO_THREE to OptionCopy(
        Icons.Filled.CalendarToday,
        stringResource(Res.string.onboarding_frequency_two_to_three_title),
        stringResource(Res.string.onboarding_frequency_two_to_three_subtitle),
    ),
    FrequencyBucket.FOUR_TO_FIVE to OptionCopy(
        Icons.Filled.CalendarMonth,
        stringResource(Res.string.onboarding_frequency_four_to_five_title),
        stringResource(Res.string.onboarding_frequency_four_to_five_subtitle),
    ),
)

@Composable
fun splitOptions(): List<Pair<SplitPreference, OptionCopy>> = listOf(
    SplitPreference.FULL_BODY to OptionCopy(
        Icons.Filled.FitnessCenter,
        stringResource(Res.string.onboarding_split_full_body_title),
        stringResource(Res.string.onboarding_split_full_body_subtitle),
    ),
    SplitPreference.UPPER_LOWER to OptionCopy(
        Icons.Filled.SwapVert,
        stringResource(Res.string.onboarding_split_upper_lower_title),
        stringResource(Res.string.onboarding_split_upper_lower_subtitle),
    ),
    SplitPreference.TIDAK_TAHU to OptionCopy(
        Icons.Filled.HelpOutline,
        stringResource(Res.string.onboarding_split_unknown_title),
        stringResource(Res.string.onboarding_split_unknown_subtitle),
    ),
)
