package com.fghilmany.nufitai.presentation.monthlyplan.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.ui.theme.NuFitColors

/** Figma node 12:617 "Quick Access Bento Section" -- navigates to Exercise Library (P-07, separate not-yet-built feature). */
@Composable
fun QuickAccessBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(NuFitColors.SecondaryFixed, RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(12.dp)),
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

/** Undesigned state (issue #76 §2 item 9) -- minimal card-based treatment reusing session-card visual language. */
@Composable
fun WeekCompleteCard(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NuFitColors.SecondaryContainer, RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Celebration, contentDescription = null, tint = NuFitColors.OnSecondaryContainer)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = NuFitColors.OnSecondaryContainer)
    }
}
