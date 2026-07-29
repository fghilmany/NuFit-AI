package com.fghilmany.nufitai.domain.exerciselibrary.entity

/**
 * Equipment category a user can select and an exercise belongs to. The first 7 values match
 * the original `Gym Techniques/` source folders exactly (ADR-002). ADR-007's data-source pivot
 * to free-exercise-db introduced 4 more (`RESISTANCE_BAND` included -- ADR-002's "intentionally
 * absent, no exercise database for it" no longer holds). The onboarding equipment picker
 * (`equipmentGridOptions()`) and Quick Assessment's "Select All" (`SELECT_ALL_EQUIPMENT`) only
 * offer the original curated 7 as toggleable cards -- do NOT assume `entries` reflects what's
 * user-selectable in those flows.
 */
enum class EquipmentCategory {
    BODYWEIGHT,
    DUMBBELL,
    BARBELL,
    KETTLEBELL,
    MACHINE_CABLE,
    PULL_UP_BAR,
    CARDIO_EQUIPMENT,
    /** ADR-007 -- new, free-exercise-db-sourced. */
    MEDICINE_BALL,
    STABILITY_BALL,
    RESISTANCE_BAND,
    ROLLER,
}
