package com.fghilmany.nufitai.domain.onboarding.entity

/**
 * Available gym equipment (issue #2 Figma node 77:146, "Apa alat yang tersedia untukmu?").
 * Multi-select; feeds the Exercise Library's movement-pool filtering downstream
 * (04-exercise-library.md) -- this layer only collects and persists the selection.
 */
enum class EquipmentType {
    BODYWEIGHT,
    DUMBBELL,
    BARBELL,
    KETTLEBELL,
    MACHINE,
    PULL_UP_BAR,
    CARDIO,
}
