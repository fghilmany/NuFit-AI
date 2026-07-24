package com.fghilmany.nufitai.presentation.fullassessment.component

import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentParQQuestionId

/** UI copy for Full Assessment's PAR-Q Kategori A+B (source: issue #29 GATE-01/GATE-02). */
val FullAssessmentParQCopy: List<Pair<FullAssessmentParQQuestionId, String>> = listOf(
    FullAssessmentParQQuestionId.Q1_NYERI_DADA to
        "Apakah Anda pernah merasa nyeri di dada saat beraktivitas fisik?",
    FullAssessmentParQQuestionId.Q2_PINGSAN_SAAT_AKTIVITAS to
        "Apakah Anda pernah pingsan atau kehilangan keseimbangan karena pusing saat beraktivitas?",
    FullAssessmentParQQuestionId.Q3_SESAK_NAPAS_AKTIVITAS_RINGAN to
        "Apakah Anda mudah sesak napas bahkan saat aktivitas ringan?",
    FullAssessmentParQQuestionId.Q4_OPERASI_BESAR_BELUM_PULIH to
        "Apakah Anda baru menjalani operasi besar dalam 12 bulan terakhir dan belum pulih sepenuhnya?",
    FullAssessmentParQQuestionId.Q5_PENGOBATAN_KANKER_AKTIF to
        "Apakah Anda sedang menjalani pengobatan aktif untuk kanker (kemoterapi/radioterapi)?",
    FullAssessmentParQQuestionId.Q6_KEHAMILAN to
        "Apakah Anda sedang hamil?",
    FullAssessmentParQQuestionId.Q7_DIABETES_TERKONTROL to
        "Apakah Anda memiliki diabetes yang sudah terkontrol?",
    FullAssessmentParQQuestionId.Q8_HIPERTENSI_TERKONTROL to
        "Apakah Anda memiliki tekanan darah tinggi yang sudah terkontrol?",
    FullAssessmentParQQuestionId.Q9_ASMA_TERKONTROL to
        "Apakah Anda memiliki asma yang sudah terkontrol?",
    FullAssessmentParQQuestionId.Q10_CEDERA_LAMA_PULIH to
        "Apakah Anda punya cedera lama yang sudah pulih tapi masih perlu diperhatikan?",
    FullAssessmentParQQuestionId.Q11_OSTEOPOROSIS_RINGAN to
        "Apakah Anda memiliki osteoporosis ringan?",
)

/** Short, plain-language label shown on the gate-blocked screen's flagged-conditions list. */
fun FullAssessmentParQQuestionId.shortLabel(): String = when (this) {
    FullAssessmentParQQuestionId.Q1_NYERI_DADA -> "Nyeri dada saat beraktivitas"
    FullAssessmentParQQuestionId.Q2_PINGSAN_SAAT_AKTIVITAS -> "Pingsan/pusing saat beraktivitas"
    FullAssessmentParQQuestionId.Q3_SESAK_NAPAS_AKTIVITAS_RINGAN -> "Sesak napas di aktivitas ringan"
    FullAssessmentParQQuestionId.Q4_OPERASI_BESAR_BELUM_PULIH -> "Operasi besar belum pulih"
    FullAssessmentParQQuestionId.Q5_PENGOBATAN_KANKER_AKTIF -> "Pengobatan kanker aktif"
    FullAssessmentParQQuestionId.Q6_KEHAMILAN -> "Sedang hamil"
    FullAssessmentParQQuestionId.Q7_DIABETES_TERKONTROL -> "Diabetes terkontrol"
    FullAssessmentParQQuestionId.Q8_HIPERTENSI_TERKONTROL -> "Hipertensi terkontrol"
    FullAssessmentParQQuestionId.Q9_ASMA_TERKONTROL -> "Asma terkontrol"
    FullAssessmentParQQuestionId.Q10_CEDERA_LAMA_PULIH -> "Cedera lama, sudah pulih"
    FullAssessmentParQQuestionId.Q11_OSTEOPOROSIS_RINGAN -> "Osteoporosis ringan"
}
