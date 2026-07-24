package com.fghilmany.nufitai.domain.fullassessment.entity

/**
 * Full Assessment's PAR-Q Kategori A+B (issue #29 GATE-01/GATE-02), distinct from onboarding's
 * `ParQQuestionId` (Quick Assessment's simpler PAR-Q-ringkas). Re-asked here because health
 * state can change between Quick Assessment (Local tier) and Full Assessment (Logged-In tier).
 * Q1-Q6 = Kategori A hard-stop (GATE-01); Q7-Q11 = Kategori B conservative-continue (GATE-02).
 */
enum class FullAssessmentParQQuestionId {
    Q1_NYERI_DADA,
    Q2_PINGSAN_SAAT_AKTIVITAS,
    Q3_SESAK_NAPAS_AKTIVITAS_RINGAN,
    Q4_OPERASI_BESAR_BELUM_PULIH,
    Q5_PENGOBATAN_KANKER_AKTIF,
    Q6_KEHAMILAN,
    Q7_DIABETES_TERKONTROL,
    Q8_HIPERTENSI_TERKONTROL,
    Q9_ASMA_TERKONTROL,
    Q10_CEDERA_LAMA_PULIH,
    Q11_OSTEOPOROSIS_RINGAN,
}
