package com.fghilmany.nufitai.presentation.onboarding.component

import com.fghilmany.nufitai.domain.onboarding.entity.ParQQuestionId

/** UI copy for each PAR-Q question, in display order (source: issue #26, Bahasa Indonesia). */
val ParQQuestionCopy: List<Pair<ParQQuestionId, String>> = listOf(
    ParQQuestionId.Q1_JANTUNG_DIAGNOSIS to
        "Apakah dokter pernah mengatakan Anda punya masalah jantung, dan menyarankan Anda hanya boleh " +
        "berolahraga sesuai anjuran dokter?",
    ParQQuestionId.Q2_NYERI_AKTIVITAS to
        "Apakah Anda pernah merasa nyeri di dada saat beraktivitas fisik (jalan cepat, naik tangga, dll)?",
    ParQQuestionId.Q3_NYERI_ISTIRAHAT to
        "Dalam sebulan terakhir, apakah Anda pernah merasa nyeri dada saat sedang tidak beraktivitas " +
        "(istirahat/duduk diam)?",
    ParQQuestionId.Q4_PUSING_PINGSAN to
        "Apakah Anda pernah kehilangan keseimbangan karena pusing, atau pernah pingsan, tanpa sebab yang jelas?",
    ParQQuestionId.Q5_TEKANAN_DARAH to
        "Apakah dokter pernah bilang tekanan darah Anda tinggi dan belum terkontrol dengan baik?",
    ParQQuestionId.Q6_MASALAH_SENDI to
        "Apakah Anda punya masalah tulang atau sendi yang bisa bertambah parah jika Anda mengubah pola " +
        "aktivitas fisik?",
    ParQQuestionId.Q7_OPERASI_RECENT to
        "Apakah Anda baru saja menjalani operasi tulang, sendi, atau tulang belakang dalam 12 bulan terakhir?",
    ParQQuestionId.Q8_OBAT_RUTIN to
        "Apakah Anda sedang mengonsumsi obat rutin untuk tekanan darah atau jantung?",
    ParQQuestionId.Q9_DIABETES to
        "Apakah Anda memiliki diabetes, dengan gula darah yang akhir-akhir ini sering naik-turun tidak stabil?",
    ParQQuestionId.Q10_KANKER_AKTIF to
        "Apakah Anda sedang menjalani pengobatan aktif untuk kanker (kemoterapi/radioterapi)?",
    ParQQuestionId.Q11_KEHAMILAN to
        "Apakah Anda sedang hamil?",
    ParQQuestionId.Q12_KONDISI_LAIN to
        "Apakah ada kondisi kesehatan lain yang perlu kami ketahui, yang belum tercakup di atas?",
)

/** Short, plain-language label shown on the consult-doctor screen's flagged-conditions list. */
fun ParQQuestionId.shortLabel(): String = when (this) {
    ParQQuestionId.Q1_JANTUNG_DIAGNOSIS -> "Riwayat masalah jantung"
    ParQQuestionId.Q2_NYERI_AKTIVITAS -> "Nyeri dada saat beraktivitas"
    ParQQuestionId.Q3_NYERI_ISTIRAHAT -> "Nyeri dada saat tidak beraktivitas (istirahat)"
    ParQQuestionId.Q4_PUSING_PINGSAN -> "Pusing atau pingsan tanpa sebab jelas"
    ParQQuestionId.Q5_TEKANAN_DARAH -> "Tekanan darah tinggi belum terkontrol"
    ParQQuestionId.Q6_MASALAH_SENDI -> "Masalah tulang atau sendi"
    ParQQuestionId.Q7_OPERASI_RECENT -> "Operasi tulang/sendi dalam 12 bulan terakhir"
    ParQQuestionId.Q8_OBAT_RUTIN -> "Obat rutin untuk jantung/tekanan darah"
    ParQQuestionId.Q9_DIABETES -> "Diabetes dengan gula darah tidak stabil"
    ParQQuestionId.Q10_KANKER_AKTIF -> "Pengobatan kanker aktif"
    ParQQuestionId.Q11_KEHAMILAN -> "Sedang hamil"
    ParQQuestionId.Q12_KONDISI_LAIN -> "Kondisi kesehatan lain"
}
