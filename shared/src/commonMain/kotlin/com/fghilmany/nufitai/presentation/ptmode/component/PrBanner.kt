package com.fghilmany.nufitai.presentation.ptmode.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MilitaryTech
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
import com.fghilmany.nufitai.domain.ptmode.entity.PersonalRecord
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.ptmode_summary_pr_body
import nufitai.shared.generated.resources.ptmode_summary_pr_delta
import nufitai.shared.generated.resources.ptmode_summary_pr_title
import org.jetbrains.compose.resources.stringResource

/** issue #80 P-06 -- "Rekor Baru!" dark banner (Figma node 12:188), one per PR this session. */
@Composable
fun PrBanner(record: PersonalRecord, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth(), backgroundColor = NuFitColors.OnPrimaryContainer) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.MilitaryTech, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Column {
                Text(stringResource(Res.string.ptmode_summary_pr_title), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text(
                    stringResource(Res.string.ptmode_summary_pr_body, record.exerciseName, formatWeightKg(record.newWeightKg)),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                val delta = record.previousBestKg?.let { record.newWeightKg - it } ?: record.newWeightKg
                Text(
                    stringResource(Res.string.ptmode_summary_pr_delta, formatWeightKg(delta)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }
    }
}
