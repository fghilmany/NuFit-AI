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
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.onboarding_equipment_barbell_subtitle
import nufitai.shared.generated.resources.onboarding_equipment_barbell_title
import nufitai.shared.generated.resources.onboarding_equipment_bodyweight_subtitle
import nufitai.shared.generated.resources.onboarding_equipment_bodyweight_title
import nufitai.shared.generated.resources.onboarding_equipment_cardio_subtitle
import nufitai.shared.generated.resources.onboarding_equipment_cardio_title
import nufitai.shared.generated.resources.onboarding_equipment_dumbbell_subtitle
import nufitai.shared.generated.resources.onboarding_equipment_dumbbell_title
import nufitai.shared.generated.resources.onboarding_equipment_kettlebell_subtitle
import nufitai.shared.generated.resources.onboarding_equipment_kettlebell_title
import nufitai.shared.generated.resources.onboarding_equipment_machine_subtitle
import nufitai.shared.generated.resources.onboarding_equipment_machine_title
import nufitai.shared.generated.resources.onboarding_equipment_pullup_bar_subtitle
import nufitai.shared.generated.resources.onboarding_equipment_pullup_bar_title
import org.jetbrains.compose.resources.stringResource

/** Icon shown in "Pilih Semua" -- not tied to a single EquipmentCategory. */
val SelectAllIcon: ImageVector = Icons.Filled.DoneAll

/** Grid-card equipment options (2-column), in Figma display order -- excludes Cardio, which gets its own full-width card. */
@Composable
fun equipmentGridOptions(): List<Pair<EquipmentCategory, OptionCopy>> = listOf(
    EquipmentCategory.BODYWEIGHT to OptionCopy(
        Icons.Filled.Accessibility,
        stringResource(Res.string.onboarding_equipment_bodyweight_title),
        stringResource(Res.string.onboarding_equipment_bodyweight_subtitle),
    ),
    EquipmentCategory.DUMBBELL to OptionCopy(
        Icons.Filled.FitnessCenter,
        stringResource(Res.string.onboarding_equipment_dumbbell_title),
        stringResource(Res.string.onboarding_equipment_dumbbell_subtitle),
    ),
    EquipmentCategory.BARBELL to OptionCopy(
        Icons.Filled.Inventory2,
        stringResource(Res.string.onboarding_equipment_barbell_title),
        stringResource(Res.string.onboarding_equipment_barbell_subtitle),
    ),
    EquipmentCategory.KETTLEBELL to OptionCopy(
        Icons.Filled.SportsBar,
        stringResource(Res.string.onboarding_equipment_kettlebell_title),
        stringResource(Res.string.onboarding_equipment_kettlebell_subtitle),
    ),
    EquipmentCategory.MACHINE_CABLE to OptionCopy(
        Icons.Filled.Tune,
        stringResource(Res.string.onboarding_equipment_machine_title),
        stringResource(Res.string.onboarding_equipment_machine_subtitle),
    ),
    EquipmentCategory.PULL_UP_BAR to OptionCopy(
        Icons.Filled.HorizontalRule,
        stringResource(Res.string.onboarding_equipment_pullup_bar_title),
        stringResource(Res.string.onboarding_equipment_pullup_bar_subtitle),
    ),
)

@Composable
fun cardioOption(): OptionCopy = OptionCopy(
    Icons.Filled.DirectionsRun,
    stringResource(Res.string.onboarding_equipment_cardio_title),
    stringResource(Res.string.onboarding_equipment_cardio_subtitle),
)
