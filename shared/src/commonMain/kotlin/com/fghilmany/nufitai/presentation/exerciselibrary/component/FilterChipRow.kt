package com.fghilmany.nufitai.presentation.exerciselibrary.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup
import com.fghilmany.nufitai.usecase.exerciselibrary.ExerciseFilter

/** P-07's 3 dropdown-style filter chips (Otot Target/Alat/Level), Figma node 12:683 (issue #79). */
@Composable
fun FilterChipRow(
    filter: ExerciseFilter,
    onToggleMuscleGroup: (MuscleGroup) -> Unit,
    onToggleEquipment: (EquipmentCategory) -> Unit,
    onToggleLevel: (ExerciseLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            label = "Otot Target",
            isActive = filter.muscleGroups.isNotEmpty(),
        ) { dismiss ->
            MuscleGroup.entries.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.shortLabel()) },
                    onClick = { onToggleMuscleGroup(value) },
                    leadingIcon = { if (value in filter.muscleGroups) Icon(Icons.Filled.Check, contentDescription = null) },
                )
            }
        }
        FilterChip(label = "Alat", isActive = filter.equipment.isNotEmpty()) {
            EquipmentCategory.entries.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.shortLabel()) },
                    onClick = { onToggleEquipment(value) },
                    leadingIcon = { if (value in filter.equipment) Icon(Icons.Filled.Check, contentDescription = null) },
                )
            }
        }
        FilterChip(label = "Level", isActive = filter.levels.isNotEmpty()) {
            BrowsableExerciseLevels.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.shortLabel()) },
                    onClick = { onToggleLevel(value) },
                    leadingIcon = { if (value in filter.levels) Icon(Icons.Filled.Check, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, isActive: Boolean, menuContent: @Composable (dismiss: () -> Unit) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    AppCard(
        modifier = Modifier.clickable { expanded = true },
        backgroundColor = if (isActive) NuFitColors.Primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            val contentColor = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            Text(label, style = MaterialTheme.typography.labelLarge, color = contentColor)
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = contentColor)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            menuContent { expanded = false }
        }
    }
}
