package com.fghilmany.nufitai.domain.onboarding.entity

/**
 * PAR-Q question -> HealthFlag mapping (source: issue #26, techspec section 5).
 * Single source of truth -- used both when a result is first submitted and when
 * an existing result is re-read from local storage.
 */
fun ParQQuestionId.toHealthFlags(): Set<HealthFlag> = when (this) {
    ParQQuestionId.Q1_JANTUNG_DIAGNOSIS,
    ParQQuestionId.Q2_NYERI_AKTIVITAS,
    ParQQuestionId.Q3_NYERI_ISTIRAHAT,
    ParQQuestionId.Q4_PUSING_PINGSAN,
    -> setOf(HealthFlag.JANTUNG)
    ParQQuestionId.Q5_TEKANAN_DARAH -> setOf(HealthFlag.TEKANAN_DARAH)
    ParQQuestionId.Q6_MASALAH_SENDI -> setOf(HealthFlag.SENDI)
    ParQQuestionId.Q7_OPERASI_RECENT -> setOf(HealthFlag.SENDI_OPERASI_RECENT)
    ParQQuestionId.Q8_OBAT_RUTIN -> setOf(HealthFlag.JANTUNG, HealthFlag.TEKANAN_DARAH)
    ParQQuestionId.Q9_DIABETES -> setOf(HealthFlag.DIABETES)
    ParQQuestionId.Q10_KANKER_AKTIF -> setOf(HealthFlag.KANKER_AKTIF)
    ParQQuestionId.Q11_KEHAMILAN -> setOf(HealthFlag.KEHAMILAN)
    ParQQuestionId.Q12_KONDISI_LAIN -> setOf(HealthFlag.LAINNYA)
}
