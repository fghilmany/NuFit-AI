package com.fghilmany.nufitai.presentation.mealplan.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors

/** P-17 B "Preferensi Jadwal" -- one selectable card (AI Random / Manual per Hari). */
@Composable
fun ScheduleModeCard(title: String, body: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        border = if (isSelected) BorderStroke(2.dp, NuFitColors.Primary) else null,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = NuFitColors.Primary)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
