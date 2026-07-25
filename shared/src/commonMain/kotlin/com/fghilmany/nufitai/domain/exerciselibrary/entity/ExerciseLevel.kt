package com.fghilmany.nufitai.domain.exerciselibrary.entity

/**
 * Normalized from `Gym Techniques`' raw level strings, which have 15+ variants
 * ("Regresi 2", "Progresi 1 (Alt)", "Regresi/Teaching", "Aksesori/Isolasi", ...).
 * [levelVariant] carries the numbered suffix, [levelNote] carries any other qualifier
 * text verbatim rather than discarding it (ADR-002).
 */
enum class ExerciseLevel {
    REGRESSION,
    STANDARD,
    PROGRESSION,
    CORRECTIVE,
    ACCESSORY,
}
