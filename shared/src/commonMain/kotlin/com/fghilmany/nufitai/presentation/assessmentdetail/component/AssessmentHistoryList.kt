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
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.ui.theme.NuFitColors

/** P-09 Riwayat tab -- not designed in Figma, reuses the "Ringkasan Jawaban" card visual language (issue #77 §9 item 4). */
@Composable
fun AssessmentHistoryList(history: List<MonthlyPlan>, modifier: Modifier = Modifier) {
    if (history.size <= 1) {
        Box(modifier = modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            Text(
                "Belum ada riwayat lain -- riwayat akan muncul setelah kamu retake atau plan baru dibuat.",
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(16.dp),
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

private fun PlanSource.shortLabel(): String = when (this) {
    PlanSource.LOCAL_TEMPLATE -> "Template"
    PlanSource.LOGGED_IN_RULE_ENGINE -> "Personalisasi"
}

private fun PlanStatus.shortLabel(): String = when (this) {
    PlanStatus.ACTIVE -> "Aktif"
    PlanStatus.ARCHIVED -> "Arsip"
}
