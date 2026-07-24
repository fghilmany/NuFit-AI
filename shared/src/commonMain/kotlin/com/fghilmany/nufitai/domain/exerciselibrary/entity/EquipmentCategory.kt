package com.fghilmany.nufitai.domain.exerciselibrary.entity

/**
 * Equipment category a user can select and an exercise belongs to. Values match the 7
 * `Gym Techniques/` source folders exactly (ADR-002) -- `RESISTANCE_BAND` is intentionally
 * absent, the source catalog has no exercise database for it yet.
 */
enum class EquipmentCategory {
    BODYWEIGHT,
    DUMBBELL,
    BARBELL,
    KETTLEBELL,
    MACHINE_CABLE,
    PULL_UP_BAR,
    CARDIO_EQUIPMENT,
}
