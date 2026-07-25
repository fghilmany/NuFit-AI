package com.fghilmany.nufitai.presentation.ptmode.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.ptmode_decrease_content_description
import nufitai.shared.generated.resources.ptmode_increase_content_description
import nufitai.shared.generated.resources.ptmode_reps_label
import nufitai.shared.generated.resources.ptmode_weight_label
import org.jetbrains.compose.resources.stringResource

private const val WEIGHT_STEP_KG = 2.5
private const val REPS_STEP = 1

/** issue #80 P-05 -- "BEBAN (KG)" / "REPETISI" +/- steppers (Figma node 12:308). */
@Composable
fun SetInputSteppers(
    weightKg: Double,
    reps: Int,
    onWeightChange: (Double) -> Unit,
    onRepsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        StepperCard(
            label = stringResource(Res.string.ptmode_weight_label),
            value = formatWeightKg(weightKg),
            onDecrease = { onWeightChange(-WEIGHT_STEP_KG) },
            onIncrease = { onWeightChange(WEIGHT_STEP_KG) },
            modifier = Modifier.weight(1f),
        )
        StepperCard(
            label = stringResource(Res.string.ptmode_reps_label),
            value = reps.toString(),
            onDecrease = { onRepsChange(-REPS_STEP) },
            onIncrease = { onRepsChange(REPS_STEP) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StepperCard(label: String, value: String, onDecrease: () -> Unit, onIncrease: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onDecrease) {
                    Icon(Icons.Filled.Remove, contentDescription = stringResource(Res.string.ptmode_decrease_content_description))
                }
                Text(value, style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onIncrease) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.ptmode_increase_content_description))
                }
            }
        }
    }
}
