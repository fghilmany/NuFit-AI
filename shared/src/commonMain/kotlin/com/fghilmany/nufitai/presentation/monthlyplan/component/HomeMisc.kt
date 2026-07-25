package com.fghilmany.nufitai.presentation.monthlyplan.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors

/** Figma node 12:617 "Quick Access Bento Section" -- navigates to Exercise Library (P-07, issue #79). */
@Composable
fun QuickAccessBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        backgroundColor = NuFitColors.SecondaryFixed,
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Explore, contentDescription = null, tint = NuFitColors.OnSecondaryFixed)
                }
                Column {
                    Text("Jelajahi Gerakan", style = MaterialTheme.typography.bodyMedium, color = NuFitColors.OnSecondaryFixed)
                    Text("Perpustakaan Latihan", style = MaterialTheme.typography.labelSmall, color = NuFitColors.OnSecondaryFixed)
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = NuFitColors.OnSecondaryFixed)
        }
    }
}

/** P-09 entry point from Home (issue #77) -- same tappable-banner shape as QuickAccessBanner above. */
@Composable
fun AssessmentDetailBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        backgroundColor = NuFitColors.SurfaceContainerLowest,
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(NuFitColors.SecondaryContainer, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Insights, contentDescription = null, tint = NuFitColors.OnSecondaryContainer)
                }
                Column {
                    Text("Hasil Assessment", style = MaterialTheme.typography.bodyMedium, color = NuFitColors.Primary)
                    Text("Lihat level & alasan plan kamu", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = NuFitColors.Primary)
        }
    }
}

/** Undesigned state (issue #76 §2 item 9) -- minimal card-based treatment reusing session-card visual language. */
@Composable
fun WeekCompleteCard(message: String, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = NuFitColors.SecondaryContainer,
        contentPadding = PaddingValues(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Celebration, contentDescription = null, tint = NuFitColors.OnSecondaryContainer)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = NuFitColors.OnSecondaryContainer)
        }
    }
}
