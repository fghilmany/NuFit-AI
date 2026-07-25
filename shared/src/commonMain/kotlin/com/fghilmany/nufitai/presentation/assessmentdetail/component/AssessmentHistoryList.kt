package com.fghilmany.nufitai.presentation.assessmentdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.assessmentdetail_history_empty
import nufitai.shared.generated.resources.assessmentdetail_history_source_personalized
import nufitai.shared.generated.resources.assessmentdetail_history_source_template
import nufitai.shared.generated.resources.assessmentdetail_history_status_active
import nufitai.shared.generated.resources.assessmentdetail_history_status_archived
import org.jetbrains.compose.resources.stringResource

/** P-09 Riwayat tab -- not designed in Figma, reuses the "Ringkasan Jawaban" card visual language (issue #77 §9 item 4). */
@Composable
fun AssessmentHistoryList(history: List<MonthlyPlan>, modifier: Modifier = Modifier) {
    if (history.size <= 1) {
        Box(modifier = modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.assessmentdetail_history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        history.forEach { plan -> AssessmentHistoryRow(plan) }
    }
}

@Composable
private fun AssessmentHistoryRow(plan: MonthlyPlan, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth(), backgroundColor = Color.White) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(plan.levelMeta, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = NuFitColors.Primary)
                Text(plan.source.shortLabel(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val badgeColor = if (plan.status == PlanStatus.ACTIVE) NuFitColors.SecondaryContainer else NuFitColors.SurfaceContainerHigh
            val badgeTextColor = if (plan.status == PlanStatus.ACTIVE) NuFitColors.OnSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            Box(modifier = Modifier.background(badgeColor, RoundedCornerShape(percent = 50)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text(plan.status.shortLabel(), style = MaterialTheme.typography.labelSmall, color = badgeTextColor)
            }
        }
    }
}

@Composable
private fun PlanSource.shortLabel(): String = when (this) {
    PlanSource.LOCAL_TEMPLATE -> stringResource(Res.string.assessmentdetail_history_source_template)
    PlanSource.LOGGED_IN_RULE_ENGINE -> stringResource(Res.string.assessmentdetail_history_source_personalized)
}

@Composable
private fun PlanStatus.shortLabel(): String = when (this) {
    PlanStatus.ACTIVE -> stringResource(Res.string.assessmentdetail_history_status_active)
    PlanStatus.ARCHIVED -> stringResource(Res.string.assessmentdetail_history_status_archived)
}
