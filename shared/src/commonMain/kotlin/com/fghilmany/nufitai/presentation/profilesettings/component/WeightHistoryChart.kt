package com.fghilmany.nufitai.presentation.profilesettings.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.profilesettings_weight_history_empty
import org.jetbrains.compose.resources.stringResource

private val CHART_HEIGHT = 96.dp
private const val MIN_BAR_FRACTION = 0.2f

/**
 * techspec profile-settings.md §9 item 4 -- last 6 raw [points] (oldest first), no aggregation.
 * Renders only the bars; the delta badge is the caller's concern (it sits in the section header,
 * not inside the chart itself, per the Figma layout).
 */
@Composable
fun WeightHistoryChart(points: List<BodyMeasurement>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) {
        Text(
            stringResource(Res.string.profilesettings_weight_history_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }

    val weights = points.map { it.weightKg ?: 0.0 }
    val min = weights.min()
    val max = weights.max()
    val range = (max - min).takeIf { it > 0.0 } ?: 1.0

    Row(
        modifier = modifier.fillMaxWidth().height(CHART_HEIGHT),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        weights.forEachIndexed { index, weight ->
            val fraction = (MIN_BAR_FRACTION + (1f - MIN_BAR_FRACTION) * ((weight - min) / range).toFloat())
                .coerceIn(MIN_BAR_FRACTION, 1f)
            val isLatest = index == weights.lastIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction)
                    .align(Alignment.Bottom)
                    .background(
                        if (isLatest) NuFitColors.Primary else NuFitColors.SecondaryContainer,
                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    ),
            )
        }
    }
}
