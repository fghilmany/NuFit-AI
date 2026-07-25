package com.fghilmany.nufitai.presentation.onboarding.component

import androidx.compose.runtime.Composable
import com.fghilmany.nufitai.domain.onboarding.entity.ParQQuestionId
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.onboarding_parq_question_diabetes
import nufitai.shared.generated.resources.onboarding_parq_question_jantung_diagnosis
import nufitai.shared.generated.resources.onboarding_parq_question_kanker_aktif
import nufitai.shared.generated.resources.onboarding_parq_question_kehamilan
import nufitai.shared.generated.resources.onboarding_parq_question_kondisi_lain
import nufitai.shared.generated.resources.onboarding_parq_question_masalah_sendi
import nufitai.shared.generated.resources.onboarding_parq_question_nyeri_aktivitas
import nufitai.shared.generated.resources.onboarding_parq_question_nyeri_istirahat
import nufitai.shared.generated.resources.onboarding_parq_question_obat_rutin
import nufitai.shared.generated.resources.onboarding_parq_question_operasi_recent
import nufitai.shared.generated.resources.onboarding_parq_question_pusing_pingsan
import nufitai.shared.generated.resources.onboarding_parq_question_tekanan_darah
import nufitai.shared.generated.resources.onboarding_parq_short_diabetes
import nufitai.shared.generated.resources.onboarding_parq_short_jantung_diagnosis
import nufitai.shared.generated.resources.onboarding_parq_short_kanker_aktif
import nufitai.shared.generated.resources.onboarding_parq_short_kehamilan
import nufitai.shared.generated.resources.onboarding_parq_short_kondisi_lain
import nufitai.shared.generated.resources.onboarding_parq_short_masalah_sendi
import nufitai.shared.generated.resources.onboarding_parq_short_nyeri_aktivitas
import nufitai.shared.generated.resources.onboarding_parq_short_nyeri_istirahat
import nufitai.shared.generated.resources.onboarding_parq_short_obat_rutin
import nufitai.shared.generated.resources.onboarding_parq_short_operasi_recent
import nufitai.shared.generated.resources.onboarding_parq_short_pusing_pingsan
import nufitai.shared.generated.resources.onboarding_parq_short_tekanan_darah
import org.jetbrains.compose.resources.stringResource

/** UI copy for each PAR-Q question, in display order (source: issue #26, Bahasa Indonesia). */
@Composable
fun parQQuestionCopy(): List<Pair<ParQQuestionId, String>> = listOf(
    ParQQuestionId.Q1_HEART_DIAGNOSIS to stringResource(Res.string.onboarding_parq_question_jantung_diagnosis),
    ParQQuestionId.Q2_CHEST_PAIN_ACTIVITY to stringResource(Res.string.onboarding_parq_question_nyeri_aktivitas),
    ParQQuestionId.Q3_CHEST_PAIN_REST to stringResource(Res.string.onboarding_parq_question_nyeri_istirahat),
    ParQQuestionId.Q4_DIZZINESS_FAINTING to stringResource(Res.string.onboarding_parq_question_pusing_pingsan),
    ParQQuestionId.Q5_BLOOD_PRESSURE to stringResource(Res.string.onboarding_parq_question_tekanan_darah),
    ParQQuestionId.Q6_JOINT_PROBLEM to stringResource(Res.string.onboarding_parq_question_masalah_sendi),
    ParQQuestionId.Q7_RECENT_SURGERY to stringResource(Res.string.onboarding_parq_question_operasi_recent),
    ParQQuestionId.Q8_ROUTINE_MEDICATION to stringResource(Res.string.onboarding_parq_question_obat_rutin),
    ParQQuestionId.Q9_DIABETES to stringResource(Res.string.onboarding_parq_question_diabetes),
    ParQQuestionId.Q10_ACTIVE_CANCER to stringResource(Res.string.onboarding_parq_question_kanker_aktif),
    ParQQuestionId.Q11_PREGNANCY to stringResource(Res.string.onboarding_parq_question_kehamilan),
    ParQQuestionId.Q12_OTHER_CONDITION to stringResource(Res.string.onboarding_parq_question_kondisi_lain),
)

/** Short, plain-language label shown on the consult-doctor screen's flagged-conditions list. */
@Composable
fun ParQQuestionId.shortLabel(): String = when (this) {
    ParQQuestionId.Q1_HEART_DIAGNOSIS -> stringResource(Res.string.onboarding_parq_short_jantung_diagnosis)
    ParQQuestionId.Q2_CHEST_PAIN_ACTIVITY -> stringResource(Res.string.onboarding_parq_short_nyeri_aktivitas)
    ParQQuestionId.Q3_CHEST_PAIN_REST -> stringResource(Res.string.onboarding_parq_short_nyeri_istirahat)
    ParQQuestionId.Q4_DIZZINESS_FAINTING -> stringResource(Res.string.onboarding_parq_short_pusing_pingsan)
    ParQQuestionId.Q5_BLOOD_PRESSURE -> stringResource(Res.string.onboarding_parq_short_tekanan_darah)
    ParQQuestionId.Q6_JOINT_PROBLEM -> stringResource(Res.string.onboarding_parq_short_masalah_sendi)
    ParQQuestionId.Q7_RECENT_SURGERY -> stringResource(Res.string.onboarding_parq_short_operasi_recent)
    ParQQuestionId.Q8_ROUTINE_MEDICATION -> stringResource(Res.string.onboarding_parq_short_obat_rutin)
    ParQQuestionId.Q9_DIABETES -> stringResource(Res.string.onboarding_parq_short_diabetes)
    ParQQuestionId.Q10_ACTIVE_CANCER -> stringResource(Res.string.onboarding_parq_short_kanker_aktif)
    ParQQuestionId.Q11_PREGNANCY -> stringResource(Res.string.onboarding_parq_short_kehamilan)
    ParQQuestionId.Q12_OTHER_CONDITION -> stringResource(Res.string.onboarding_parq_short_kondisi_lain)
}
