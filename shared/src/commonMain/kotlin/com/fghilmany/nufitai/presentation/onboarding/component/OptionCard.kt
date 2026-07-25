package com.fghilmany.nufitai.presentation.onboarding.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors

/**
 * Card-style single-select option matching the Quick Assessment wizard's Figma pattern
 * (node 12:117): 48dp icon in a mint-tinted rounded square, 20sp semibold title, 12sp subtitle.
 */
@Composable
fun OptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(2.dp, Color.Transparent)
        },
        elevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NuFitColors.SecondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NuFitColors.OnSecondaryContainer,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = NuFitColors.Primary)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
