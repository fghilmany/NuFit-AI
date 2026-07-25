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
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.usecase.monthlyplan.SessionStatus

/** P-03's 4 card variants (Figma node 12:539: Completed/Today-Active/Rest/Upcoming), 1:1 with [SessionStatus]. */
@Composable
fun SessionCard(day: PlanDay, status: SessionStatus, onClick: () -> Unit, onStartClick: () -> Unit, modifier: Modifier = Modifier) {
    when (status) {
        SessionStatus.TODAY -> TodayCard(day, onStartClick, modifier)
        SessionStatus.REST, SessionStatus.LIGHT_ACTIVITY -> RestCard(day, status, onClick, modifier)
        else -> StandardCard(day, status, onClick, modifier)
    }
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
                    Text("HARI INI", style = MaterialTheme.typography.labelSmall, color = NuFitColors.PrimaryContainer)
                }
                Text(day.templateLetter?.let { "Sesi $it" } ?: "Sesi Hari Ini", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${day.exerciseCount()} gerakan", style = MaterialTheme.typography.labelSmall, color = Color.White)
                Text("${day.estimatedDurationMinutes()} menit", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
            AppButton(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = NuFitColors.PrimaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Mulai Latihan") }
        }
    }
}

@Composable
private fun StandardCard(day: PlanDay, status: SessionStatus, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isCompleted = status == SessionStatus.DONE
    AppElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().alpha(if (isCompleted) 0.75f else 1f),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(modifier = Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (isCompleted) NuFitColors.SecondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompleted) Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = NuFitColors.OnSecondaryContainer)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(day.templateLetter?.let { "Sesi $it" } ?: "Sesi", style = MaterialTheme.typography.titleLarge)
                    StatusPill(status)
                }
                Text("${day.exerciseCount()} gerakan • ${day.estimatedDurationMinutes()} menit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RestCard(day: PlanDay, status: SessionStatus, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (day.type == DayType.LIGHT_ACTIVITY) "Aktivitas Ringan" else "Rest Day", style = MaterialTheme.typography.titleLarge)
                    StatusPill(status)
                }
                Text("Santai sejenak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusPill(status: SessionStatus) {
    val (bg, fg, label) = when (status) {
        SessionStatus.DONE -> Triple(NuFitColors.SecondaryContainer, NuFitColors.OnSecondaryContainer, "Selesai")
        SessionStatus.SKIPPED -> Triple(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant, "Dilewati")
        SessionStatus.UPCOMING -> Triple(MaterialTheme.colorScheme.surfaceContainerHighest, NuFitColors.Outline, "Akan datang")
        SessionStatus.REST, SessionStatus.LIGHT_ACTIVITY -> Triple(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant, "Besok")
        SessionStatus.TODAY -> Triple(NuFitColors.OnPrimaryContainer, NuFitColors.PrimaryContainer, "Hari Ini")
    }
    Box(modifier = Modifier.background(bg, RoundedCornerShape(percent = 50)).padding(horizontal = 12.dp, vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}
