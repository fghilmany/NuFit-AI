package com.fghilmany.nufitai.presentation.assessmentdetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.presentation.onboarding.component.EquipmentGridOptions
import com.fghilmany.nufitai.presentation.onboarding.component.ExperienceOptions
import com.fghilmany.nufitai.presentation.onboarding.component.FrequencyOptions
import com.fghilmany.nufitai.presentation.onboarding.component.GoalOptions
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.assessmentdetail_answer_summary_title
import nufitai.shared.generated.resources.assessmentdetail_equipment_bodyweight_only
import nufitai.shared.generated.resources.assessmentdetail_equipment_full_gym
import nufitai.shared.generated.resources.assessmentdetail_equipment_none
import nufitai.shared.generated.resources.assessmentdetail_full_assessment_cta_body
import nufitai.shared.generated.resources.assessmentdetail_full_assessment_cta_button
import nufitai.shared.generated.resources.assessmentdetail_full_assessment_cta_title
import nufitai.shared.generated.resources.assessmentdetail_level_explainer
import nufitai.shared.generated.resources.assessmentdetail_parq_title
import nufitai.shared.generated.resources.assessmentdetail_plan_badge_ai
import nufitai.shared.generated.resources.assessmentdetail_plan_badge_template
import nufitai.shared.generated.resources.assessmentdetail_plan_description
import nufitai.shared.generated.resources.assessmentdetail_retake_footer_link
import nufitai.shared.generated.resources.assessmentdetail_status_label
import nufitai.shared.generated.resources.assessmentdetail_summary_equipment
import nufitai.shared.generated.resources.assessmentdetail_summary_experience
import nufitai.shared.generated.resources.assessmentdetail_summary_frequency
import nufitai.shared.generated.resources.assessmentdetail_summary_goal
import org.jetbrains.compose.resources.stringResource

/** Figma node 12:994 "Top Section: Beginner Badge & Explainer" (issue #77). */
@Composable
fun LevelBadgeCard(quickAssessment: QuickAssessmentResult, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = NuFitColors.SecondaryContainer,
        contentPadding = PaddingValues(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(Res.string.assessmentdetail_status_label), style = MaterialTheme.typography.labelMedium, color = NuFitColors.Primary)
            Text(
                quickAssessment.level.shortLabel(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = NuFitColors.Primary,
            )
            AppCard(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                backgroundColor = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(17.dp),
            ) {
                val goalTitle = GoalOptions.first { it.first == quickAssessment.input.goal }.second.title
                val frequencyTitle = FrequencyOptions.first { it.first == quickAssessment.input.frequency }.second.title
                Text(
                    stringResource(
                        Res.string.assessmentdetail_level_explainer,
                        quickAssessment.level.shortLabel(),
                        goalTitle,
                        frequencyTitle,
                        quickAssessment.resolvedSplit.shortLabel(),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Figma node 12:1004 "PAR-Q Safety Section" -- AC-3, one card per active flag (issue #77). */
@Composable
fun FlagExplanationCard(label: String, impact: String, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NuFitColors.Warning.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
        backgroundColor = NuFitColors.WarningContainer,
        contentPadding = PaddingValues(17.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(38.dp).background(NuFitColors.Warning, RoundedCornerShape(percent = 50)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = NuFitColors.OnWarning)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.assessmentdetail_parq_title), style = MaterialTheme.typography.labelLarge, color = NuFitColors.OnWarningContainer)
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NuFitColors.OnWarningContainerBody)
                Text(impact, style = MaterialTheme.typography.bodyMedium, color = NuFitColors.OnWarningContainerBody)
            }
        }
    }
}

/** Figma node 12:1016 "Ringkasan Jawaban" 2x2 grid (issue #77). */
@Composable
fun AnswerSummaryGrid(quickAssessment: QuickAssessmentResult, modifier: Modifier = Modifier) {
    val goalTitle = GoalOptions.first { it.first == quickAssessment.input.goal }.second.title
    val frequencyTitle = FrequencyOptions.first { it.first == quickAssessment.input.frequency }.second.title
    val experienceTitle = ExperienceOptions.first { it.first == quickAssessment.input.experience }.second.title
    val equipmentSummary = quickAssessment.input.equipment.summaryLabel()

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCell(stringResource(Res.string.assessmentdetail_summary_goal), goalTitle, Modifier.weight(1f))
            SummaryCell(stringResource(Res.string.assessmentdetail_summary_frequency), frequencyTitle, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCell(stringResource(Res.string.assessmentdetail_summary_experience), experienceTitle, Modifier.weight(1f))
            SummaryCell(stringResource(Res.string.assessmentdetail_summary_equipment), equipmentSummary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryCell(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, backgroundColor = Color.White) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = NuFitColors.Primary)
        }
    }
}

@Composable
private fun Set<EquipmentCategory>.summaryLabel(): String {
    val fullGymSet = EquipmentGridOptions.map { it.first }.toSet() - EquipmentCategory.BODYWEIGHT
    return when {
        isEmpty() -> stringResource(Res.string.assessmentdetail_equipment_none)
        this == setOf(EquipmentCategory.BODYWEIGHT) -> stringResource(Res.string.assessmentdetail_equipment_bodyweight_only)
        fullGymSet.all { it in this } -> stringResource(Res.string.assessmentdetail_equipment_full_gym)
        else -> EquipmentGridOptions.filter { it.first in this }.joinToString(", ") { it.second.title }
    }
}

/** Figma node 12:1040 "Rencana Terpilih" -- no bundled hero image yet, uses a solid tone instead (issue #77). */
@Composable
fun SelectedPlanCard(quickAssessment: QuickAssessmentResult, plan: MonthlyPlan, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = NuFitColors.PrimaryContainer,
        contentPadding = PaddingValues(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val badgeLabel = when (plan.source) {
                PlanSource.LOCAL_TEMPLATE -> stringResource(Res.string.assessmentdetail_plan_badge_template)
                PlanSource.LOGGED_IN_RULE_ENGINE -> stringResource(Res.string.assessmentdetail_plan_badge_ai)
            }
            AppCard(
                backgroundColor = NuFitColors.TertiaryFixed,
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(badgeLabel, style = MaterialTheme.typography.labelSmall, color = NuFitColors.OnTertiaryFixed)
            }
            Text(
                "${quickAssessment.resolvedSplit.shortLabel()} ${quickAssessment.level.shortLabel()}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                stringResource(Res.string.assessmentdetail_plan_description),
                style = MaterialTheme.typography.bodyMedium,
                color = NuFitColors.OnPrimaryContainer,
            )
        }
    }
}

/** Figma node 12:1051 "Assessment Belum Lengkap" CTA -- AC-2: never rendered as an error state (issue #77). */
@Composable
fun FullAssessmentCtaCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, NuFitColors.OutlineVariant, RoundedCornerShape(24.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(64.dp).background(NuFitColors.TertiaryFixed.copy(alpha = 0.2f), RoundedCornerShape(percent = 50)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = NuFitColors.Primary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(Res.string.assessmentdetail_full_assessment_cta_title), style = MaterialTheme.typography.titleLarge, color = NuFitColors.Primary)
            Text(
                stringResource(Res.string.assessmentdetail_full_assessment_cta_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AppButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.assessmentdetail_full_assessment_cta_button))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

/** Figma node 12:1064 "Ulangi Assessment" footer link (issue #77). */
@Composable
fun RetakeFooterLink(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Refresh, contentDescription = null, tint = NuFitColors.Primary, modifier = Modifier.size(16.dp))
        Text(stringResource(Res.string.assessmentdetail_retake_footer_link), style = MaterialTheme.typography.labelLarge, color = NuFitColors.Primary)
    }
}
