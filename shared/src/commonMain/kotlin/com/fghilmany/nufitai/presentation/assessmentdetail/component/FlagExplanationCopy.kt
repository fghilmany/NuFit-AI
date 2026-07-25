package com.fghilmany.nufitai.presentation.assessmentdetail.component

import androidx.compose.runtime.Composable
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.onboarding.entity.HealthFlag
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.assessmentdetail_flag_diabetes_impact
import nufitai.shared.generated.resources.assessmentdetail_flag_diabetes_label
import nufitai.shared.generated.resources.assessmentdetail_flag_health_impact
import nufitai.shared.generated.resources.assessmentdetail_flag_jantung_impact
import nufitai.shared.generated.resources.assessmentdetail_flag_jantung_label
import nufitai.shared.generated.resources.assessmentdetail_flag_kanker_impact
import nufitai.shared.generated.resources.assessmentdetail_flag_kanker_label
import nufitai.shared.generated.resources.assessmentdetail_flag_kehamilan_impact
import nufitai.shared.generated.resources.assessmentdetail_flag_kehamilan_label
import nufitai.shared.generated.resources.assessmentdetail_flag_lainnya_impact
import nufitai.shared.generated.resources.assessmentdetail_flag_lainnya_label
import nufitai.shared.generated.resources.assessmentdetail_flag_movement_impact
import nufitai.shared.generated.resources.assessmentdetail_flag_postural_impact
import nufitai.shared.generated.resources.assessmentdetail_flag_sendi_impact
import nufitai.shared.generated.resources.assessmentdetail_flag_sendi_label
import nufitai.shared.generated.resources.assessmentdetail_flag_sendi_operasi_impact
import nufitai.shared.generated.resources.assessmentdetail_flag_sendi_operasi_label
import nufitai.shared.generated.resources.assessmentdetail_flag_tekanan_darah_impact
import nufitai.shared.generated.resources.assessmentdetail_flag_tekanan_darah_label
import org.jetbrains.compose.resources.stringResource

/** AC-3 (03-assessment-detail.md, issue #77): plain-language label for a Quick-tier PAR-Q flag. */
@Composable
fun HealthFlag.shortLabel(): String = when (this) {
    HealthFlag.JANTUNG -> stringResource(Res.string.assessmentdetail_flag_jantung_label)
    HealthFlag.TEKANAN_DARAH -> stringResource(Res.string.assessmentdetail_flag_tekanan_darah_label)
    HealthFlag.SENDI -> stringResource(Res.string.assessmentdetail_flag_sendi_label)
    HealthFlag.SENDI_OPERASI_RECENT -> stringResource(Res.string.assessmentdetail_flag_sendi_operasi_label)
    HealthFlag.DIABETES -> stringResource(Res.string.assessmentdetail_flag_diabetes_label)
    HealthFlag.KANKER_AKTIF -> stringResource(Res.string.assessmentdetail_flag_kanker_label)
    HealthFlag.KEHAMILAN -> stringResource(Res.string.assessmentdetail_flag_kehamilan_label)
    HealthFlag.LAINNYA -> stringResource(Res.string.assessmentdetail_flag_lainnya_label)
}

/** AC-3: plain-language impact-on-plan sentence for a Quick-tier PAR-Q flag. */
@Composable
fun HealthFlag.explanationImpact(): String = when (this) {
    HealthFlag.JANTUNG -> stringResource(Res.string.assessmentdetail_flag_jantung_impact)
    HealthFlag.TEKANAN_DARAH -> stringResource(Res.string.assessmentdetail_flag_tekanan_darah_impact)
    HealthFlag.SENDI -> stringResource(Res.string.assessmentdetail_flag_sendi_impact)
    HealthFlag.SENDI_OPERASI_RECENT -> stringResource(Res.string.assessmentdetail_flag_sendi_operasi_impact)
    HealthFlag.DIABETES -> stringResource(Res.string.assessmentdetail_flag_diabetes_impact)
    HealthFlag.KANKER_AKTIF -> stringResource(Res.string.assessmentdetail_flag_kanker_impact)
    HealthFlag.KEHAMILAN -> stringResource(Res.string.assessmentdetail_flag_kehamilan_impact)
    HealthFlag.LAINNYA -> stringResource(Res.string.assessmentdetail_flag_lainnya_impact)
}

/** AC-3: plain-language impact-on-plan sentence for a Full-tier flag (health/postural/movement). */
@Composable
fun ExerciseFlag.explanationImpact(): String = when (this) {
    ExerciseFlag.HEALTH_JANTUNG,
    ExerciseFlag.HEALTH_TEKANAN_DARAH,
    ExerciseFlag.HEALTH_DIABETES,
    ExerciseFlag.HEALTH_ASMA,
    ExerciseFlag.HEALTH_SENDI,
    ExerciseFlag.HEALTH_OSTEOPOROSIS,
    -> stringResource(Res.string.assessmentdetail_flag_health_impact)

    ExerciseFlag.POSTURAL_FORWARD_HEAD,
    ExerciseFlag.POSTURAL_ROUNDED_SHOULDER,
    ExerciseFlag.POSTURAL_KYPHOSIS,
    ExerciseFlag.POSTURAL_APT,
    ExerciseFlag.POSTURAL_PPT,
    ExerciseFlag.POSTURAL_ASIMETRI_BAHU,
    ExerciseFlag.POSTURAL_ASIMETRI_PINGGUL,
    -> stringResource(Res.string.assessmentdetail_flag_postural_impact)

    ExerciseFlag.MOVEMENT_ANKLE_MOBILITY,
    ExerciseFlag.MOVEMENT_KNEE_VALGUS,
    ExerciseFlag.MOVEMENT_HINGE_FROM_BACK,
    ExerciseFlag.MOVEMENT_SHOULDER_MOBILITY,
    ExerciseFlag.MOVEMENT_CORE_INSTABILITY,
    ExerciseFlag.MOVEMENT_BALANCE_ASIMETRI,
    -> stringResource(Res.string.assessmentdetail_flag_movement_impact)
}
