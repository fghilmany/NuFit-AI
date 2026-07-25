package com.fghilmany.nufitai.domain.exerciselibrary.entity

/**
 * Joint/area receiving mechanical load (NOT the muscle trained -- e.g. squat loads
 * `KNEE` + `LOWER_BACK` even though the muscles worked are quad/glute). Phase-1
 * scope per `Gym Techniques/database-gerakan-dikelompokkan-per-kategori-pola-gerak.md`;
 * pergelangan_tangan/pergelangan_kaki are a documented future phase, not modeled yet.
 */
enum class BodyArea {
    LOWER_BACK,
    KNEE,
    SHOULDER,
    NECK,
}
