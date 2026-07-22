package com.fghilmany.nufitai.presentation.onboarding.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.fghilmany.nufitai.domain.onboarding.entity.EquipmentType

/** Icon shown in "Pilih Semua" -- not tied to a single EquipmentType. */
val SelectAllIcon: ImageVector = Icons.Filled.DoneAll

/** Grid-card equipment options (2-column), in Figma display order -- excludes Cardio, which gets its own full-width card. */
val EquipmentGridOptions: List<Pair<EquipmentType, OptionCopy>> = listOf(
    EquipmentType.BODYWEIGHT to OptionCopy(Icons.Filled.Accessibility, "Bodyweight", "Tanpa Alat"),
    EquipmentType.DUMBBELL to OptionCopy(Icons.Filled.FitnessCenter, "Dumbbell", "Sepasang Alat"),
    EquipmentType.BARBELL to OptionCopy(Icons.Filled.Inventory2, "Barbell", "Angkat Beban"),
    EquipmentType.KETTLEBELL to OptionCopy(Icons.Filled.SportsBar, "Kettlebell", "Beban Ayun"),
    EquipmentType.MACHINE to OptionCopy(Icons.Filled.Tune, "Machine", "Cable & Station"),
    EquipmentType.PULL_UP_BAR to OptionCopy(Icons.Filled.HorizontalRule, "Pull-Up Bar", "Tiang Angkat"),
)

val CardioOption = OptionCopy(Icons.Filled.DirectionsRun, "Cardio", "Treadmill, Sepeda, dll")
