package com.fghilmany.nufitai.presentation.assessmentdetail.component

import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.onboarding.entity.HealthFlag

/** AC-3 (03-assessment-detail.md, issue #77): plain-language label for a Quick-tier PAR-Q flag. */
fun HealthFlag.shortLabel(): String = when (this) {
    HealthFlag.JANTUNG -> "Riwayat jantung"
    HealthFlag.TEKANAN_DARAH -> "Tekanan darah"
    HealthFlag.SENDI -> "Riwayat nyeri sendi"
    HealthFlag.SENDI_OPERASI_RECENT -> "Operasi sendi baru-baru ini"
    HealthFlag.DIABETES -> "Diabetes"
    HealthFlag.KANKER_AKTIF -> "Pengobatan kanker aktif"
    HealthFlag.KEHAMILAN -> "Kehamilan"
    HealthFlag.LAINNYA -> "Kondisi lain"
}

/** AC-3: plain-language impact-on-plan sentence for a Quick-tier PAR-Q flag. */
fun HealthFlag.explanationImpact(): String = when (this) {
    HealthFlag.JANTUNG -> "Gerakan intensitas tinggi yang membebani jantung akan dihindari atau dimodifikasi dari rencana latihanmu."
    HealthFlag.TEKANAN_DARAH -> "Gerakan yang menahan napas atau membebani tekanan darah akan dimodifikasi dari rencana latihanmu."
    HealthFlag.SENDI -> "Gerakan high-impact seperti Jumping Jacks akan difilter dari rencana latihanmu."
    HealthFlag.SENDI_OPERASI_RECENT -> "Gerakan yang membebani area operasi akan dihindari sampai kamu benar-benar pulih."
    HealthFlag.DIABETES -> "Intensitas latihan disesuaikan agar tetap aman untuk kondisi gula darahmu."
    HealthFlag.KANKER_AKTIF -> "Latihan disesuaikan agar tetap ringan dan aman selama masa pengobatan."
    HealthFlag.KEHAMILAN -> "Gerakan berisiko tinggi untuk kehamilan akan dihindari dari rencana latihanmu."
    HealthFlag.LAINNYA -> "Rencana latihanmu disesuaikan secara umum untuk berjaga-jaga."
}

/** AC-3: plain-language impact-on-plan sentence for a Full-tier flag (health/postural/movement). */
fun ExerciseFlag.explanationImpact(): String = when (this) {
    ExerciseFlag.HEALTH_JANTUNG,
    ExerciseFlag.HEALTH_TEKANAN_DARAH,
    ExerciseFlag.HEALTH_DIABETES,
    ExerciseFlag.HEALTH_ASMA,
    ExerciseFlag.HEALTH_SENDI,
    ExerciseFlag.HEALTH_OSTEOPOROSIS,
    -> "Gerakan yang berisiko untuk kondisi ini akan dihindari atau dimodifikasi dari rencana latihanmu."

    ExerciseFlag.POSTURAL_FORWARD_HEAD,
    ExerciseFlag.POSTURAL_ROUNDED_SHOULDER,
    ExerciseFlag.POSTURAL_KYPHOSIS,
    ExerciseFlag.POSTURAL_APT,
    ExerciseFlag.POSTURAL_PPT,
    ExerciseFlag.POSTURAL_ASIMETRI_BAHU,
    ExerciseFlag.POSTURAL_ASIMETRI_PINGGUL,
    -> "Latihan korektif akan ditambahkan dan gerakan yang memperparah postur ini akan disesuaikan."

    ExerciseFlag.MOVEMENT_ANKLE_MOBILITY,
    ExerciseFlag.MOVEMENT_KNEE_VALGUS,
    ExerciseFlag.MOVEMENT_HINGE_FROM_BACK,
    ExerciseFlag.MOVEMENT_SHOULDER_MOBILITY,
    ExerciseFlag.MOVEMENT_CORE_INSTABILITY,
    ExerciseFlag.MOVEMENT_BALANCE_ASIMETRI,
    -> "Gerakan akan disesuaikan atau diberi variasi yang lebih aman untuk pola gerakmu."
}
