package com.fghilmany.nufitai.presentation.fullassessment.component

import androidx.compose.runtime.Composable
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.fullassessment_flag_health_asma
import nufitai.shared.generated.resources.fullassessment_flag_health_diabetes
import nufitai.shared.generated.resources.fullassessment_flag_health_jantung
import nufitai.shared.generated.resources.fullassessment_flag_health_osteoporosis
import nufitai.shared.generated.resources.fullassessment_flag_health_sendi
import nufitai.shared.generated.resources.fullassessment_flag_health_tekanan_darah
import nufitai.shared.generated.resources.fullassessment_flag_movement_ankle_mobility
import nufitai.shared.generated.resources.fullassessment_flag_movement_balance_asimetri
import nufitai.shared.generated.resources.fullassessment_flag_movement_core_instability
import nufitai.shared.generated.resources.fullassessment_flag_movement_hinge_from_back
import nufitai.shared.generated.resources.fullassessment_flag_movement_knee_valgus
import nufitai.shared.generated.resources.fullassessment_flag_movement_shoulder_mobility
import nufitai.shared.generated.resources.fullassessment_flag_postural_apt
import nufitai.shared.generated.resources.fullassessment_flag_postural_asimetri_bahu
import nufitai.shared.generated.resources.fullassessment_flag_postural_asimetri_pinggul
import nufitai.shared.generated.resources.fullassessment_flag_postural_forward_head
import nufitai.shared.generated.resources.fullassessment_flag_postural_kyphosis
import nufitai.shared.generated.resources.fullassessment_flag_postural_ppt
import nufitai.shared.generated.resources.fullassessment_flag_postural_rounded_shoulder
import org.jetbrains.compose.resources.stringResource

/** Plain-language labels for the postural/movement screening checklists (stub scope). */
@Composable
fun ExerciseFlag.shortLabel(): String = when (this) {
    ExerciseFlag.HEALTH_HEART -> stringResource(Res.string.fullassessment_flag_health_jantung)
    ExerciseFlag.HEALTH_BLOOD_PRESSURE -> stringResource(Res.string.fullassessment_flag_health_tekanan_darah)
    ExerciseFlag.HEALTH_DIABETES -> stringResource(Res.string.fullassessment_flag_health_diabetes)
    ExerciseFlag.HEALTH_ASTHMA -> stringResource(Res.string.fullassessment_flag_health_asma)
    ExerciseFlag.HEALTH_JOINT -> stringResource(Res.string.fullassessment_flag_health_sendi)
    ExerciseFlag.HEALTH_OSTEOPOROSIS -> stringResource(Res.string.fullassessment_flag_health_osteoporosis)
    ExerciseFlag.POSTURAL_FORWARD_HEAD -> stringResource(Res.string.fullassessment_flag_postural_forward_head)
    ExerciseFlag.POSTURAL_ROUNDED_SHOULDER -> stringResource(Res.string.fullassessment_flag_postural_rounded_shoulder)
    ExerciseFlag.POSTURAL_KYPHOSIS -> stringResource(Res.string.fullassessment_flag_postural_kyphosis)
    ExerciseFlag.POSTURAL_APT -> stringResource(Res.string.fullassessment_flag_postural_apt)
    ExerciseFlag.POSTURAL_PPT -> stringResource(Res.string.fullassessment_flag_postural_ppt)
    ExerciseFlag.POSTURAL_SHOULDER_ASYMMETRY -> stringResource(Res.string.fullassessment_flag_postural_asimetri_bahu)
    ExerciseFlag.POSTURAL_HIP_ASYMMETRY -> stringResource(Res.string.fullassessment_flag_postural_asimetri_pinggul)
    ExerciseFlag.MOVEMENT_ANKLE_MOBILITY -> stringResource(Res.string.fullassessment_flag_movement_ankle_mobility)
    ExerciseFlag.MOVEMENT_KNEE_VALGUS -> stringResource(Res.string.fullassessment_flag_movement_knee_valgus)
    ExerciseFlag.MOVEMENT_HINGE_FROM_BACK -> stringResource(Res.string.fullassessment_flag_movement_hinge_from_back)
    ExerciseFlag.MOVEMENT_SHOULDER_MOBILITY -> stringResource(Res.string.fullassessment_flag_movement_shoulder_mobility)
    ExerciseFlag.MOVEMENT_CORE_INSTABILITY -> stringResource(Res.string.fullassessment_flag_movement_core_instability)
    ExerciseFlag.MOVEMENT_BALANCE_ASYMMETRY -> stringResource(Res.string.fullassessment_flag_movement_balance_asimetri)
}
