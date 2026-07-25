package com.fghilmany.nufitai.presentation.fullassessment.component

import androidx.compose.runtime.Composable
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentParQQuestionId
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.fullassessment_parq_q1_nyeri_dada
import nufitai.shared.generated.resources.fullassessment_parq_q2_pingsan_saat_aktivitas
import nufitai.shared.generated.resources.fullassessment_parq_q3_sesak_napas_aktivitas_ringan
import nufitai.shared.generated.resources.fullassessment_parq_q4_operasi_besar_belum_pulih
import nufitai.shared.generated.resources.fullassessment_parq_q5_pengobatan_kanker_aktif
import nufitai.shared.generated.resources.fullassessment_parq_q6_kehamilan
import nufitai.shared.generated.resources.fullassessment_parq_q7_diabetes_terkontrol
import nufitai.shared.generated.resources.fullassessment_parq_q8_hipertensi_terkontrol
import nufitai.shared.generated.resources.fullassessment_parq_q9_asma_terkontrol
import nufitai.shared.generated.resources.fullassessment_parq_q10_cedera_lama_pulih
import nufitai.shared.generated.resources.fullassessment_parq_q11_osteoporosis_ringan
import nufitai.shared.generated.resources.fullassessment_parq_short_asma_terkontrol
import nufitai.shared.generated.resources.fullassessment_parq_short_cedera_lama_pulih
import nufitai.shared.generated.resources.fullassessment_parq_short_diabetes_terkontrol
import nufitai.shared.generated.resources.fullassessment_parq_short_hipertensi_terkontrol
import nufitai.shared.generated.resources.fullassessment_parq_short_kehamilan
import nufitai.shared.generated.resources.fullassessment_parq_short_nyeri_dada
import nufitai.shared.generated.resources.fullassessment_parq_short_operasi_besar_belum_pulih
import nufitai.shared.generated.resources.fullassessment_parq_short_osteoporosis_ringan
import nufitai.shared.generated.resources.fullassessment_parq_short_pengobatan_kanker_aktif
import nufitai.shared.generated.resources.fullassessment_parq_short_pingsan_saat_aktivitas
import nufitai.shared.generated.resources.fullassessment_parq_short_sesak_napas_aktivitas_ringan
import org.jetbrains.compose.resources.stringResource

/** UI copy for Full Assessment's PAR-Q Kategori A+B (source: issue #29 GATE-01/GATE-02). */
@Composable
fun fullAssessmentParQCopy(): List<Pair<FullAssessmentParQQuestionId, String>> = listOf(
    FullAssessmentParQQuestionId.Q1_NYERI_DADA to
        stringResource(Res.string.fullassessment_parq_q1_nyeri_dada),
    FullAssessmentParQQuestionId.Q2_PINGSAN_SAAT_AKTIVITAS to
        stringResource(Res.string.fullassessment_parq_q2_pingsan_saat_aktivitas),
    FullAssessmentParQQuestionId.Q3_SESAK_NAPAS_AKTIVITAS_RINGAN to
        stringResource(Res.string.fullassessment_parq_q3_sesak_napas_aktivitas_ringan),
    FullAssessmentParQQuestionId.Q4_OPERASI_BESAR_BELUM_PULIH to
        stringResource(Res.string.fullassessment_parq_q4_operasi_besar_belum_pulih),
    FullAssessmentParQQuestionId.Q5_PENGOBATAN_KANKER_AKTIF to
        stringResource(Res.string.fullassessment_parq_q5_pengobatan_kanker_aktif),
    FullAssessmentParQQuestionId.Q6_KEHAMILAN to
        stringResource(Res.string.fullassessment_parq_q6_kehamilan),
    FullAssessmentParQQuestionId.Q7_DIABETES_TERKONTROL to
        stringResource(Res.string.fullassessment_parq_q7_diabetes_terkontrol),
    FullAssessmentParQQuestionId.Q8_HIPERTENSI_TERKONTROL to
        stringResource(Res.string.fullassessment_parq_q8_hipertensi_terkontrol),
    FullAssessmentParQQuestionId.Q9_ASMA_TERKONTROL to
        stringResource(Res.string.fullassessment_parq_q9_asma_terkontrol),
    FullAssessmentParQQuestionId.Q10_CEDERA_LAMA_PULIH to
        stringResource(Res.string.fullassessment_parq_q10_cedera_lama_pulih),
    FullAssessmentParQQuestionId.Q11_OSTEOPOROSIS_RINGAN to
        stringResource(Res.string.fullassessment_parq_q11_osteoporosis_ringan),
)

/** Short, plain-language label shown on the gate-blocked screen's flagged-conditions list. */
@Composable
fun FullAssessmentParQQuestionId.shortLabel(): String = when (this) {
    FullAssessmentParQQuestionId.Q1_NYERI_DADA -> stringResource(Res.string.fullassessment_parq_short_nyeri_dada)
    FullAssessmentParQQuestionId.Q2_PINGSAN_SAAT_AKTIVITAS ->
        stringResource(Res.string.fullassessment_parq_short_pingsan_saat_aktivitas)
    FullAssessmentParQQuestionId.Q3_SESAK_NAPAS_AKTIVITAS_RINGAN ->
        stringResource(Res.string.fullassessment_parq_short_sesak_napas_aktivitas_ringan)
    FullAssessmentParQQuestionId.Q4_OPERASI_BESAR_BELUM_PULIH ->
        stringResource(Res.string.fullassessment_parq_short_operasi_besar_belum_pulih)
    FullAssessmentParQQuestionId.Q5_PENGOBATAN_KANKER_AKTIF ->
        stringResource(Res.string.fullassessment_parq_short_pengobatan_kanker_aktif)
    FullAssessmentParQQuestionId.Q6_KEHAMILAN -> stringResource(Res.string.fullassessment_parq_short_kehamilan)
    FullAssessmentParQQuestionId.Q7_DIABETES_TERKONTROL ->
        stringResource(Res.string.fullassessment_parq_short_diabetes_terkontrol)
    FullAssessmentParQQuestionId.Q8_HIPERTENSI_TERKONTROL ->
        stringResource(Res.string.fullassessment_parq_short_hipertensi_terkontrol)
    FullAssessmentParQQuestionId.Q9_ASMA_TERKONTROL -> stringResource(Res.string.fullassessment_parq_short_asma_terkontrol)
    FullAssessmentParQQuestionId.Q10_CEDERA_LAMA_PULIH ->
        stringResource(Res.string.fullassessment_parq_short_cedera_lama_pulih)
    FullAssessmentParQQuestionId.Q11_OSTEOPOROSIS_RINGAN ->
        stringResource(Res.string.fullassessment_parq_short_osteoporosis_ringan)
}
