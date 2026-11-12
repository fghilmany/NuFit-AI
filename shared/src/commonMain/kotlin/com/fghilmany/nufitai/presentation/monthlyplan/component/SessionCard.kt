package com.fghilmany.nufitai.presentation.monthlyplan.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.core.designsystem.component.BundledMediaImage
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.usecase.monthlyplan.SessionStatus
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.fullassessment_day_fri
import nufitai.shared.generated.resources.fullassessment_day_mon
import nufitai.shared.generated.resources.fullassessment_day_sat
import nufitai.shared.generated.resources.fullassessment_day_sun
import nufitai.shared.generated.resources.fullassessment_day_thu
import nufitai.shared.generated.resources.fullassessment_day_tue
import nufitai.shared.generated.resources.fullassessment_day_wed
import nufitai.shared.generated.resources.monthlyplan_duration_minutes_label
import nufitai.shared.generated.resources.monthlyplan_exercise_count_label
import nufitai.shared.generated.resources.monthlyplan_session_card_fallback
import nufitai.shared.generated.resources.monthlyplan_session_card_light_activity_title
import nufitai.shared.generated.resources.monthlyplan_session_card_rest_day_title
import nufitai.shared.generated.resources.monthlyplan_session_card_rest_subtitle
import nufitai.shared.generated.resources.monthlyplan_session_card_session_letter
import nufitai.shared.generated.resources.monthlyplan_session_card_start_button
import nufitai.shared.generated.resources.monthlyplan_session_card_summary_line
import nufitai.shared.generated.resources.monthlyplan_session_card_today_badge
import nufitai.shared.generated.resources.monthlyplan_session_card_today_fallback
import nufitai.shared.generated.resources.monthlyplan_status_done
import nufitai.shared.generated.resources.monthlyplan_status_skipped
import nufitai.shared.generated.resources.monthlyplan_status_today
import nufitai.shared.generated.resources.monthlyplan_status_tomorrow
import nufitai.shared.generated.resources.monthlyplan_status_upcoming
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * P-03's 4 card variants (Figma node 12:539: Completed/Today-Active/Rest/Upcoming), 1:1 with
 * [SessionStatus]. [planStartedAt] + [PlanDay.dayNumber] derive each card's real calendar day
 * label -- TodayCard skips it (today is already explicit via its badge/title). [now] is used
 * alongside them to tell apart "hari ini"/"besok"/"akan datang" on the pill -- REST/LIGHT_ACTIVITY
 * never resolve to [SessionStatus.TODAY] (GetSessionStatus checks day type before the date), so
 * a rest day landing on today still needs its own day-accurate pill label, not a blanket one.
 */
@Composable
fun SessionCard(
    day: PlanDay,
    status: SessionStatus,
    planStartedAt: Instant,
    now: Instant,
    exercise: Exercise?,
    onClick: () -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (status) {
        SessionStatus.TODAY -> TodayCard(day, onStartClick, modifier)
        SessionStatus.REST, SessionStatus.LIGHT_ACTIVITY -> RestCard(day, status, planStartedAt, now, onClick, modifier)
        else -> StandardCard(day, status, planStartedAt, now, exercise, onClick, modifier)
    }
}

/** 0 = today, 1 = tomorrow, 2+ = further out. Mirrors [com.fghilmany.nufitai.usecase.monthlyplan.GetSessionStatus]'s own `todayDayNumber` math. */
private fun dayOffset(planStartedAt: Instant, now: Instant, dayNumber: Int): Int {
    val todayDayNumber = (now - planStartedAt).inWholeDays.toInt() + 1
    return dayNumber - todayDayNumber
}

@Composable
private fun DayOfWeek.shortLabel(): String = when (this) {
    DayOfWeek.MONDAY -> stringResource(Res.string.fullassessment_day_mon)
    DayOfWeek.TUESDAY -> stringResource(Res.string.fullassessment_day_tue)
    DayOfWeek.WEDNESDAY -> stringResource(Res.string.fullassessment_day_wed)
    DayOfWeek.THURSDAY -> stringResource(Res.string.fullassessment_day_thu)
    DayOfWeek.FRIDAY -> stringResource(Res.string.fullassessment_day_fri)
    DayOfWeek.SATURDAY -> stringResource(Res.string.fullassessment_day_sat)
    DayOfWeek.SUNDAY -> stringResource(Res.string.fullassessment_day_sun)
}

/** "Sen, 12/11" -- numeric date, no month-name formatter exists anywhere in this codebase to
 * mirror (see WeekNavigator's same deliberate simplification). */
@Composable
private fun dayLabel(planStartedAt: Instant, dayNumber: Int): String {
    val date = (planStartedAt + (dayNumber - 1).days).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.dayOfWeek.shortLabel()}, ${date.dayOfMonth}/${date.monthNumber}"
}

@Composable
private fun TodayCard(day: PlanDay, onStartClick: () -> Unit, modifier: Modifier = Modifier) {
    AppElevatedCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = NuFitColors.PrimaryContainer,
        elevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(
                    modifier = Modifier.background(NuFitColors.OnPrimaryContainer, RoundedCornerShape(percent = 50)).padding(horizontal = 12.dp, vertical = 3.dp),
                ) {
                    Text(stringResource(Res.string.monthlyplan_session_card_today_badge), style = MaterialTheme.typography.labelSmall, color = NuFitColors.PrimaryContainer)
                }
                Text(
                    day.templateLetter?.let { stringResource(Res.string.monthlyplan_session_card_session_letter, it) }
                        ?: stringResource(Res.string.monthlyplan_session_card_today_fallback),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(Res.string.monthlyplan_exercise_count_label, day.exerciseCount()), style = MaterialTheme.typography.labelSmall, color = Color.White)
                Text(stringResource(Res.string.monthlyplan_duration_minutes_label, day.estimatedDurationMinutes()), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
            AppButton(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = NuFitColors.PrimaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(Res.string.monthlyplan_session_card_start_button)) }
        }
    }
}

@Composable
private fun StandardCard(day: PlanDay, status: SessionStatus, planStartedAt: Instant, now: Instant, exercise: Exercise?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isCompleted = status == SessionStatus.DONE
    AppElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().alpha(if (isCompleted) 0.75f else 1f),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(modifier = Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))) {
                BundledMediaImage(
                    resourcePath = exercise?.imagePath,
                    contentDescription = exercise?.name,
                    modifier = Modifier.size(64.dp),
                )
                if (isCompleted) {
                    Box(
                        modifier = Modifier.size(64.dp).background(NuFitColors.SecondaryContainer.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = NuFitColors.OnSecondaryContainer)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(dayLabel(planStartedAt, day.dayNumber), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        day.templateLetter?.let { stringResource(Res.string.monthlyplan_session_card_session_letter, it) }
                            ?: stringResource(Res.string.monthlyplan_session_card_fallback),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    StatusPill(status, dayOffset(planStartedAt, now, day.dayNumber))
                }
                Text(
                    stringResource(Res.string.monthlyplan_session_card_summary_line, day.exerciseCount(), day.estimatedDurationMinutes()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RestCard(day: PlanDay, status: SessionStatus, planStartedAt: Instant, now: Instant, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(modifier = Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(NuFitColors.TertiaryFixedDim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                val icon = if (status == SessionStatus.LIGHT_ACTIVITY) Icons.Filled.DirectionsRun else Icons.Filled.Bedtime
                Icon(icon, contentDescription = null, tint = NuFitColors.OnTertiaryFixed)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(dayLabel(planStartedAt, day.dayNumber), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        if (day.type == DayType.LIGHT_ACTIVITY) {
                            stringResource(Res.string.monthlyplan_session_card_light_activity_title)
                        } else {
                            stringResource(Res.string.monthlyplan_session_card_rest_day_title)
                        },
                        style = MaterialTheme.typography.titleLarge,
                    )
                    StatusPill(status, dayOffset(planStartedAt, now, day.dayNumber))
                }
                Text(stringResource(Res.string.monthlyplan_session_card_rest_subtitle), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** [dayOffset]: 0 = today, 1 = tomorrow, 2+ = further out -- only read for UPCOMING/REST/LIGHT_ACTIVITY. */
@Composable
private fun StatusPill(status: SessionStatus, dayOffset: Int = 0) {
    val (bg, fg, label) = when (status) {
        SessionStatus.DONE -> Triple(NuFitColors.SecondaryContainer, NuFitColors.OnSecondaryContainer, stringResource(Res.string.monthlyplan_status_done))
        SessionStatus.SKIPPED -> Triple(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant, stringResource(Res.string.monthlyplan_status_skipped))
        SessionStatus.UPCOMING -> Triple(MaterialTheme.colorScheme.surfaceContainerHighest, NuFitColors.Outline, dayOffsetLabel(dayOffset))
        SessionStatus.REST, SessionStatus.LIGHT_ACTIVITY -> Triple(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant, dayOffsetLabel(dayOffset))
        SessionStatus.TODAY -> Triple(NuFitColors.OnPrimaryContainer, NuFitColors.PrimaryContainer, stringResource(Res.string.monthlyplan_status_today))
    }
    Box(modifier = Modifier.background(bg, RoundedCornerShape(percent = 50)).padding(horizontal = 12.dp, vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}

@Composable
private fun dayOffsetLabel(dayOffset: Int): String = when (dayOffset) {
    0 -> stringResource(Res.string.monthlyplan_status_today)
    1 -> stringResource(Res.string.monthlyplan_status_tomorrow)
    else -> stringResource(Res.string.monthlyplan_status_upcoming)
}
