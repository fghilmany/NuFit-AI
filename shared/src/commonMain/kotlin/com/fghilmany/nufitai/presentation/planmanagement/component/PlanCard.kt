package com.fghilmany.nufitai.presentation.planmanagement.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.presentation.onboarding.component.ProgressBar
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.planmanagement_action_activate
import nufitai.shared.generated.resources.planmanagement_action_delete
import nufitai.shared.generated.resources.planmanagement_action_restore
import nufitai.shared.generated.resources.planmanagement_badge_active
import nufitai.shared.generated.resources.planmanagement_badge_archived
import nufitai.shared.generated.resources.planmanagement_badge_draft
import nufitai.shared.generated.resources.planmanagement_source_ai
import nufitai.shared.generated.resources.planmanagement_source_personalized
import nufitai.shared.generated.resources.planmanagement_source_template
import org.jetbrains.compose.resources.stringResource

/** P-14 plan card -- status+source badges, "Aktifkan"/"Pulihkan"/"Hapus" per Figma node 12:1251. */
@Composable
fun PlanCard(
    plan: MonthlyPlan,
    progressPercent: Int?,
    isBusy: Boolean,
    onActivateOrRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isActive = plan.status == PlanStatus.ACTIVE
    val isArchived = plan.status == PlanStatus.ARCHIVED
    val contentAlpha = if (isArchived) 0.5f else 1f

    AppElevatedCard(
        modifier = modifier.fillMaxWidth(),
        border = if (isActive) BorderStroke(2.dp, NuFitColors.Primary) else null,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(plan.status)
                SourceBadge(plan.source)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    plan.planName?.takeIf { it.isNotBlank() } ?: plan.levelMeta,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                )
                if (isActive) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = NuFitColors.Primary)
                }
            }

            if (isActive && progressPercent != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProgressBar(progress = progressPercent / 100f, modifier = Modifier.weight(1f))
                    Text("$progressPercent%", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (!isActive) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onActivateOrRestore, enabled = !isBusy) {
                        Text(stringResource(if (isArchived) Res.string.planmanagement_action_restore else Res.string.planmanagement_action_activate))
                    }
                    TextButton(onClick = onDelete, enabled = !isBusy) {
                        Text(stringResource(Res.string.planmanagement_action_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: PlanStatus) {
    val (labelRes, color) = when (status) {
        PlanStatus.ACTIVE -> Res.string.planmanagement_badge_active to NuFitColors.Primary
        PlanStatus.DRAFT -> Res.string.planmanagement_badge_draft to MaterialTheme.colorScheme.surfaceContainerHigh
        PlanStatus.ARCHIVED -> Res.string.planmanagement_badge_archived to MaterialTheme.colorScheme.surfaceContainerHigh
    }
    Box(modifier = Modifier.background(color, RoundedCornerShape(percent = 50))) {
        Text(
            stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = if (status == PlanStatus.ACTIVE) NuFitColors.OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun SourceBadge(source: PlanSource) {
    val labelRes = when (source) {
        PlanSource.LOCAL_TEMPLATE -> Res.string.planmanagement_source_template
        PlanSource.LOGGED_IN_RULE_ENGINE -> Res.string.planmanagement_source_personalized
        PlanSource.AI_GENERATED -> Res.string.planmanagement_source_ai
    }
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(percent = 50))) {
        Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp))
    }
}
