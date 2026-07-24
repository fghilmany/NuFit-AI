package com.fghilmany.nufitai.domain.exerciselibrary.entity

/**
 * The 19-flag taxonomy from issue #29's "Matriks Cakupan Flag x Rule" -- the single source
 * of truth for `Exercise.flagExclusion`/`flagPrioritas` and for every SAFE-* rule's `flag`
 * matcher. Grouped by source stage; `flag_kehamilan` is deliberately excluded, per #29 it
 * never reaches program-level filtering (GATE-01 hard-stop only, Full Assessment's PAR-Q
 * gate -- see `usecase/fullassessment/SubmitFullAssessmentParQ.kt`).
 *
 * Distinct from onboarding's `HealthFlag` (Quick Assessment's lighter PAR-Q-ringkas gate) --
 * the 6 `HEALTH_*` values here are Full Assessment's PAR-Q Kategori B taxonomy, which doesn't
 * map 1:1 onto `HealthFlag` (adds ASMA/OSTEOPOROSIS, drops KANKER_AKTIF/KEHAMILAN/LAINNYA
 * which are GATE-01 hard-stops that never become an exercise-filtering flag).
 */
enum class ExerciseFlag {
    // Tahap 0 Kategori B (health)
    HEALTH_JANTUNG,
    HEALTH_TEKANAN_DARAH,
    HEALTH_DIABETES,
    HEALTH_ASMA,
    HEALTH_SENDI,
    HEALTH_OSTEOPOROSIS,

    // Tahap 3 (postural)
    POSTURAL_FORWARD_HEAD,
    POSTURAL_ROUNDED_SHOULDER,
    POSTURAL_KYPHOSIS,
    POSTURAL_APT,
    POSTURAL_PPT,
    POSTURAL_ASIMETRI_BAHU,
    POSTURAL_ASIMETRI_PINGGUL,

    // Tahap 4 (movement screening)
    MOVEMENT_ANKLE_MOBILITY,
    MOVEMENT_KNEE_VALGUS,
    MOVEMENT_HINGE_FROM_BACK,
    MOVEMENT_SHOULDER_MOBILITY,
    MOVEMENT_CORE_INSTABILITY,
    MOVEMENT_BALANCE_ASIMETRI,
}
