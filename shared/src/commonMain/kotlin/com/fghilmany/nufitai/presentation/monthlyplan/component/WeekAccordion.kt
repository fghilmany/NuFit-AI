package com.fghilmany.nufitai.presentation.monthlyplan.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors

/** Collapsed accordion row for a non-current week (Figma node 12:629 "Collapsed Accordion Sections"). */
@Composable
fun WeekAccordionHeader(weekNumber: Int, isExpanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Minggu $weekNumber", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(
                if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = NuFitColors.Outline,
            )
        }
    }
}
